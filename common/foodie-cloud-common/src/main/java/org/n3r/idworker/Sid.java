package org.n3r.idworker;

import org.n3r.idworker.strategy.DefaultWorkerIdStrategy;
import org.n3r.idworker.utils.Utils;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class Sid {
    private static WorkerIdStrategy workerIdStrategy;
    private static IdWorker idWorker;

    static {
        configure(DefaultWorkerIdStrategy.instance);
    }

    public static synchronized void configure(WorkerIdStrategy custom) {
        if (workerIdStrategy != null) workerIdStrategy.release();
        workerIdStrategy = custom;
        /** 这里加上  workerIdStrategy.initialize(); 可以做到根据不同的机器生成不同的id--满足分布式全局唯一id的要求：看分析时评*/
        workerIdStrategy.initialize();
        idWorker = new IdWorker(workerIdStrategy.availableWorkerId()) {
            @Override
            public long getEpoch() {
                return Utils.midnightMillis();
            }
        };
    }

    /**
     * 一天最大毫秒86400000，最大占用27比特
     * 27+10+11=48位 最大值281474976710655(15字)，YK0XXHZ827(10字)
     * 6位(YYMMDD)+15位，共21位
     *
     * @return 固定21位数字字符串
     */

    public static String next() {
        long id = idWorker.nextId();
        String yyMMdd = new SimpleDateFormat("yyMMdd").format(new Date());
        return yyMMdd + String.format("%014d", id);
    }


    /**
     * 返回固定16位的字母数字混编的字符串。
     */
    public String nextShort() {
        long id = idWorker.nextId();
        String yyMMdd = new SimpleDateFormat("yyMMdd").format(new Date());
        return yyMMdd + Utils.padLeft(Utils.encode(id), 10, '0');
    }
    
//    public static void main(String[] args) {
//		String aa = new Sid().nextShort();
//		String bb = new Sid().next();
//
//        /**  长id和短id
//         * 210329CPKTX318X4
//         * 210329134738408898560
//         */
//		System.out.println(aa);
//		System.out.println(bb);
//	}
}
