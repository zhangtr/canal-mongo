package com.torry.data.common;

/**
 * 枚举类
 */
public class EnumEntity {


    /**
     * 	交易类型枚举类
     */
    public enum TransTypeEnum {
        CONSUME("consume","消费"){},
        CONSUMECANCEL("consumeCancel","消费撤销"){},
        REVERSAL("reversal","冲正"){},
        PREAUTHORIZATION("preAuthorization","预授权"){},
        PREAUTHORIZATIONCANCEL("preAuthorizationCancel","预授权撤销"){},
        PREAUTHORIZATIONFIN("preAuthorizationFin","预授权完成"){},
        PREAUTHORIZATIONFINCANCEL("preAuthorizationFinCancel","预授权完成撤销"){},
        PREAUTHORIZATIONREVERSAL("preAuthorizationReversal","预授权冲正"){},
        PREAUTHORIZATIONFINREVERSAL("preAuthorizationFinReversal","预授权完成冲正"){},
        REFUND("refund","退货"){},
        QRORDERCLOSE("qrOrderClose","码支付订单关闭"){};
        private String value;
        private String desc;
        TransTypeEnum(String value, String desc)
        {
            this.value = value;
            this.desc=desc;
        }

        public String getValue()
        {
            return value;
        }

        public String getDesc() {
            return desc;
        }

        public static TransTypeEnum getTransType(String value){
            for(TransTypeEnum v : TransTypeEnum.values()){
                if(value.equals(v.getValue())){
                    return v;
                }
            }
            return null;
        }

    }

    /**
     * 	交易状态枚举类
     */
    public enum TransStatusEnum {
        INIT("init","初始化"){},
        SUCCESS("success","成功"){},
        FAILURE("failure","失败"){},
        TSPROCESSING("tsProcessing","TS预处理"){},
        PROCESSING("processing","处理中"){};
        private String value;
        private String desc;
        TransStatusEnum(String value, String desc)
        {
            this.value = value;
            this.desc=desc;
        }

        public String getValue()
        {
            return value;
        }

        public String getDesc() {
            return desc;
        }
        public static TransStatusEnum getTransStatus(String value){
            for(TransStatusEnum v : TransStatusEnum.values()){
                if(value.equals(v.getValue())){
                    return v;
                }
            }
            return null;
        }
    }

    /**
     * 	卡类型枚举类
     */
    public enum CardTypeEnum {
        DEBIT("1","借记卡"){},
        CREDIT("2","贷记卡"){},
        SEMICREDIT("3","准贷记卡"){},
        PREPAY("4","预付费卡"){};
        private String value;
        private String desc;
        CardTypeEnum(String value, String desc)
        {
            this.value = value;
            this.desc=desc;
        }

        public String getValue()
        {
            return value;
        }

        public String getDesc() {
            return desc;
        }
        public static CardTypeEnum getCardType(String value){
            for(CardTypeEnum v : CardTypeEnum.values()){
                if(value.equals(v.getValue())){
                    return v;
                }
            }
            return null;
        }
    }

    /**
     * 	订单来源枚举类
     */
    public enum SourceEnum {
        QPOS("QPOS","QPOS"){},
        POS("POS","大POS"){},
        QPAY("QPAY","Q刷"){},
        MPOS("MPOS","MPOS"){};
        private String value;
        private String desc;
        SourceEnum(String value, String desc)
        {
            this.value = value;
            this.desc=desc;
        }

        public String getValue()
        {
            return value;
        }

        public String getDesc() {
            return desc;
        }
        public static SourceEnum getSource(String value){
            for(SourceEnum v : SourceEnum.values()){
                if(value.equals(v.getValue())){
                    return v;
                }
            }
            return null;
        }
    }
    /**
     * 	支付方式枚举类
     */
    public enum PayTypeEnum {
        POSPAY("POSPAY","刷卡"){},
        QR_WECHAT("QR_WECHAT","微信"){},
        QR_ALIPAY("QR_ALIPAY","支付宝"){},
        QR_UNIONPAY("QR_UNIONPAY","银联二维码"){},
        QR_JDPAY("QR_JDPAY","京东钱包"){},
        QR_QQPAY("QR_QQPAY","QQ钱包"){};
        private String value;
        private String desc;
        PayTypeEnum(String value, String desc)
        {
            this.value = value;
            this.desc=desc;
        }

        public String getValue()
        {
            return value;
        }

        public String getDesc() {
            return desc;
        }
        public static PayTypeEnum getPayType(String value){
            for(PayTypeEnum v : PayTypeEnum.values()){
                if(value.equals(v.getValue())){
                    return v;
                }
            }
            return null;
        }
    }

    /**
     * 	卡类型枚举类
     */
    public enum OrderStatusEnum {
        CANCELED("canceled","交易取消"){},
        REVERSED("reversed","已冲正"){},
        REFUNDED("refunded","已退货"){};
        private String value;
        private String desc;
        OrderStatusEnum(String value, String desc)
        {
            this.value = value;
            this.desc=desc;
        }

        public String getValue()
        {
            return value;
        }

        public String getDesc() {
            return desc;
        }
        public static CardTypeEnum getCardType(String value){
            for(CardTypeEnum v : CardTypeEnum.values()){
                if(value.equals(v.getValue())){
                    return v;
                }
            }
            return null;
        }
    }
}
