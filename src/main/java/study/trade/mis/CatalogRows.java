package study.trade.mis;

import java.math.BigDecimal;

public final class CatalogRows {
    private CatalogRows() {
    }

    public static class Brand {
        public long id;
        public String brandName;
        public String website;
        public long productCount;
    }

    public static class Order {
        public long id;
        public String orderNo;
        public String customerName;
        public String orderedOn;
        public String orderStatus;
    }

    public static class Line {
        public long id;
        public long productId;
        public String productName;
        public int quantity;
        public BigDecimal dealPrice;
        public BigDecimal subtotal;
    }
}
