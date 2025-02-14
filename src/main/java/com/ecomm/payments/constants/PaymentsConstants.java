package com.ecomm.payments.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentsConstants {

    public static final String IDEMPOTENCY_KEY = "idempotency-key";
    public static final String SITE_ID = "siteId";
    public static final String API_KEY = "x-api-key";
    public static final String CARD_MASK = "XXXXXX";
    public static final String AMEX_CARD_MASK = "XXXXX";
    public static final String EXPIRY_DATE_DELIMITER = "/";

    public static final String CARD_ON_FILE = "CardOnFile";
    public static final String UNSCHEDULED_CARD_ON_FILE = "UnscheduledCardOnFile";
    public static final String DATE_FORMAT_HOUR_MIN_SEC_MILL = "HHmmssSSS";
    public static final String REFRENCE_PREFIX_CC = "cc";

    public static final String RISK_BASKET_ITEM = "riskdata.basket.item";
    public static final String REQUESTED_TEST_ACQUIRER_RC = "RequestedTestAcquirerResponseCode";
    public static final String RISK_SHIPPING_METHOD = "riskdata.shippingMethod";
    public static final String RISK_LINE_ITEMS = "riskdata.lineItems";
    public static final String RISK_ORDER_DISCOUNT = "riskdata.orderDiscount";
    public static final String RISK_TOTAL_ITEMS = "riskdata.totalItems";
    public static final String RISK_CHANNEL = "riskdata.channel";
    public static final String RISK_COUPON_QUANTITY = "riskdata.couponQuantity";
    public static final String RISK_LOYALTY = "riskdata.loyalty";
    public static final String RISK_CART_TYPE = "riskdata.cartType";
    public static final String PROMOTION_CODE = "riskdata.promotionCode";
    public static final String WEB_STORE_ID = "webStoreID";
    public static final String TIME_ON_FILE = "riskdata.timeOnFile";
    public static final String ALTERNATE_PAYMENT_TYPE = "riskdata.alternatePaymentType";
    public static final String RISK_SHIPPING_ADDRESS_NEIGHBORHOOD = "riskdata.neighborhood";
    public static final String RISK_PICKUP_PERSON_EMAIL = "riskdata.pickupemail";
    public static final String RISK_PICKUP_PERSON_FIRSTNAME = "riskdata.pickupfirstname";
    public static final String RISK_PICKUP_PERSON_LASTNAME = "riskdata.pickuplastname";
    public static final String RISK_PICKUP_PERSON_PHONE = "riskdata.pickupphone";
    public static final String RISK_PICKUP_STREET_ADDRESS = "riskdata.pickupstreetaddress";
    public static final String RISK_PICKUP_CITY = "riskdata.pickupcity";
    public static final String RISK_PICKUP_STATE = "riskdata.pickupstate";
    public static final String RISK_PICKUP_ZIPCODE = "riskdata.pickupzipcode";
    public static final String RISK_PICKUP_COUNTRY = "riskdata.pickupcountry";

    public static final String PAYPAL_SUB_TYPE = "sdk";
    public static final String MEXICAN_CURRENCY = "MXN";
    public static final String DETAILS = "details";
    public static final String EXECUTE_3D = "executeThreeD";
    public static final String CHANNEL_WEB = "web";
    public static final String PAYMENT_FIELD = "payment";

    public static final String CART_TYPE_REGULAR = "REGULAR";
    public static final String CART_TYPE_VGC = "VGC";
    public static final String PROMO = "PROMO";
    public static final String SHIPPING_METHOD_BOPIS = "BPS";

    public static final String WEB = "Web";
    public static final String STR_TRUE = "true";
    public static final String ERROR_CODE_503 = "503";
    public static final String ERROR_KEY = "error.internalException";
    public static final String OXXO_PAYMENT_RETURN_URL = "https://www.ae.com/checkout";
    public static final String GREEN = "GREEN";

    public static final double DOUBLE_HUNDRED = 100.00;
    public static final double DOUBLE_ZERO = 0.0D;
    public static final String ZERO = "0";

    public static final String ENHANCEDSCHEMEDATA_TOTAL_TAXAMOUNT = "enhancedSchemeData.totalTaxAmount";
    public static final String ENHANCEDSCHEMEDATA_CUSTOMER_REFERENCE = "enhancedSchemeData.customerReference";
    public static final String ENHANCEDSCHEMEDATA_ITEMDETAILLINE = "enhancedSchemeData.itemDetailLine";
    public static final String PRODUCT_CODE = "productCode";
    public static final String COMMODITY_CODE = "commodityCode";
    public static final String DESCRIPTION = "description";
    public static final String QUANTITY = "quantity";
    public static final String UNIT_OF_MEASURE = "unitOfMeasure";
    public static final String UNIT_PRICE = "unitPrice";
    public static final String TOTAL_AMOUNT = "totalAmount";
    public static final String DISCOUNT_AMOUNT = "discountAmount";

    public static final String HYPEN = "-";
    public static final String DOT = ".";
    public static final String COMMA = ",";
    public static final String COUPON_CODE_SEPARATOR = "[,|]";
    public static final String YES = "y";
    public static final String NO = "n";

    public static final String PAYMENTS = "/payments";
    public static final String EMPTY_STRING = "";
    public static final String DEBIT = "DEBIT";
    public static final String CREDIT = "CREDIT";
    public static final String INTEGER_FORMAT = "{0,number,0}";
    public static final int INT_HUNDRED = 100;
    public static final String VOID = "VOID";
    public static final String DEFAULT_FRAUD_RESULT_TYPE_GREEN = "GREEN";
    public static final String DEFAULT_RESULT_CODE_TIMEOUT = "Timeout";
    public static final String SINGLE_SPACE = " ";

    public static final String AEO_MX = "AEO_MX";

    public static final String SHIP_CODE_STD = "STD";
    public static final String EXPRESS = "EXPRESS";
    public static final String STANDARD = "STANDARD";

    public static final String HEADER_BASIC = "Basic";
    public static final String COLON = ":";

    public static final String COUNTRY_CA = "CA";
    public static final String COUNTRY_US = "US";
    public static final String PLUS_SIGN = "+";

    public static final String LOG_KEY_PAYMENT_METHOD = "payment_method";
    public static final String LOG_KEY_IDEMPOTENCY_KEY = "idempotencyKey";
    public static final String LOG_KEY_MERCHANT_ACCOUNT = "merchantAccount";
    public static final String LOG_KEY_SITE_ID = "site_id";
    public static final String LOG_KEY_CHANNEL = "channel";
    public static final String LOG_KEY_REFERENCE = "reference";
    public static final String LOG_KEY_WEBSTORE_ID = "webStoreID";
    public static final String LOG_KEY_MERCHANT_REFERENCE = "merchantReference";
    public static final String LOG_KEY_RESULT_CODE = "resultCode";
    public static final String LOG_KEY_FRAUD_RESULT_TYPE = "fraudResultType";
    public static final String LOG_KEY_PAYMENT_HEADER_ID = "payment_header_id";
    public static final String LOG_KEY_METHOD_NAME = "method_name";
    public static final String LOG_KEY_AUTH_PENDING_FLAG = "authPendingFlag";
    public static final String LOG_KEY_EMAIL = "email";
    public static final String LOG_KEY_CART_ID = "cart_id";
    public static final String LOG_KEY_ORDER_NUMBER = "order_number";
    public static final String LOG_KEY_AUTHORIZATION_REQUEST = "authorization_request";
    public static final String LOG_KEY_AUTHORIZATION_RESPONSE = "authorization_response";
    public static final String LOG_KEY_DETAIL_REQUEST = "detail_request";
    public static final String LOG_KEY_DETAIL_RESPONSE = "detail_response";
    public static final String LOG_KEY_CHECKOUT_TYPE = "checkout_type";

    public static final String AUTH_FAILED_FIELD = "authorizationFailed";
    public static final String AUTH_TXN_TYPE = "auth";
    public static final String AUTH_TXN_SERVICE = "auth-service";

    public static final String AUTHORIZED = "AUTHORIZED";
    public static final String CANCELLED = "CANCELLED";
    public static final String PENDING = "PENDING";
    public static final String DECLINED = "DECLINED";
    public static final String AUTH_PENDING = "AUTH_PENDING";
    public static final String AUTH_REFUSED = "AUTHORIZE_REFUSED";
    public static final String REDIRECT_SHOPPER = "REDIRECT_SHOPPER";
    public static final String REDIRECT_AFTERPAY = "REDIRECT_AFTERPAY";
    public static final String AUTH_APPROVED = "AUTH_APPROVED";
    public static final String AUTHORIZATION = "authorization";
    public static final String SETTLED = "SETTLED";
    public static final String CAPTURED = "CAPTURED";

    public static final String IDENTIFIER_PAYMENTS = "Payments";
    public static final String IDENTIFIER_ADYEN = "Adyen";
    public static final String PAYMENT_METHOD_AFTERPAY = "AFTERPAY";
    public static final String PAYMENT_METHOD_CASHAPP = "CASHAPP";
    public static final String OXXO_PAYMENT_METHOD = "oxxo";
    public static final String PAYPAL_PAYMENT_METHOD = "paypal";
    public static final String CREDIT_CARD_TYPE = "creditCard";
    public static final String CREDIT_CARD = "creditcard";
    public static final String GIFT_CARD = "giftcard";
    public static final String APPLE_PAY = "applepay";

    public static final String CHECKOUT_TYPE_REGULAR = "regular";
    public static final String CHECKOUT_TYPE_EXPRESS = "EXPRESS";

    public static final String GATEWAY_AFTERPAY = "AFTERPAY";
    public static final String GATEWAY_BRAINTREE = "BRAINTREE";

    public static final String ERROR_MESSAGE = "Service Unavailble";
    public static final String SERVICE_UNAVAILABLE = "Service Unavailable";
    public static final String INVALID_ORDER_NUMBER = "Invalid Order Number";
    public static final String ERROR_PAYMENT_HEADER_DTO_NOT_AVAILABLE_MESSAGE = "PaymentHeaderDTO not available in payment auth database for orderNumber";
    public static final String ADYEN_SHOPPER_INTERACTION_ECOMMERCE = "Ecommerce";
    public static final String ADYEN_SHOPPER_INTERACTION_CONTAUTH = "ContAuth";

}
