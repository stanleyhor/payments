package com.aeo.utils;

import com.aeo.model.cucumber_model.AuthorizationRequestInfo;
import com.aeo.model.request.Address;
import com.aeo.model.request.AltPayDetails;
import com.aeo.model.request.BrowserInfo;
import com.aeo.model.request.CommerceItem;
import com.aeo.model.request.ContactInfo;
import com.aeo.model.request.FraudDetail;
import com.aeo.model.request.OrderSummary;
import com.aeo.model.request.ShippingDetails;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Random;

public class TestDataUtils {

    public static OrderSummary buildOrderSummary(
            AuthorizationRequestInfo authorizationRequestInfo, String orderIdFromContext, Double totalSum, Double giftCardTotal
    ) {
        return OrderSummary.builder()
                .orderNumber(
                        "randomNumber".equals(authorizationRequestInfo.getOrderNumber()) ? generateOrderNumber() : authorizationRequestInfo.getOrderNumber())
                .checkoutLocale(null)
                .cartId(setCartId(authorizationRequestInfo.getCartId(), orderIdFromContext))
                .currency(authorizationRequestInfo.getCurrencyCode())
                .giftCardTotal(String.valueOf(getGiftCardTotal(giftCardTotal, authorizationRequestInfo)))
                .amountToBeAuthorized(getGiftCardTotal(giftCardTotal, authorizationRequestInfo))
                .orderTotal(getTotalSum(totalSum, authorizationRequestInfo))
                .channelType(authorizationRequestInfo.getChannelType())
                .returnUrl(authorizationRequestInfo.getReturnUrl())
                .profileId(authorizationRequestInfo.getProfileId())
                .taxTotal(0.0)
                .orderDiscountAmount("0.0")
                .checkoutType("REGULAR")
                .build();
    }

    private static String generateOrderNumber() {
        Random random = new Random();
        int orderNumberInt = random.nextInt(1_000_000_000, 1_009_999_999);
        return String.valueOf(orderNumberInt);
    }

    private static String setCartId(String cartId, String orderIdFromContext) {
        return "fromContext".equalsIgnoreCase(cartId) ? orderIdFromContext : cartId;
    }

    public static ShippingDetails buildShippingDetails(AuthorizationRequestInfo authorizationRequestInfo) {
        return ShippingDetails.builder()
                .shippingAddress(buildShippingAddress(authorizationRequestInfo))
                .shippingMethod(authorizationRequestInfo.getShippingMethod())
                .shippingEmail("")
                .contactInfo(ContactInfo.builder()
                        .email(authorizationRequestInfo.getEmail())
                        .phoneNo(authorizationRequestInfo.getPhoneNo())
                        .build())
                .build();
    }

    public static Address buildShippingAddress(AuthorizationRequestInfo authorizationRequestInfo) {
        return Address.builder()
                .address1(authorizationRequestInfo.getAddress1())
                .address2("")
                .city(authorizationRequestInfo.getCity())
                .country(authorizationRequestInfo.getCountry())
                .firstName(authorizationRequestInfo.getFirstName())
                .lastName(authorizationRequestInfo.getLastName())
                .neighborhood("")
                .postalCode(authorizationRequestInfo.getPostalCode())
                .state(authorizationRequestInfo.getState())
                .build();
    }

    public static FraudDetail buildAdyenFraudDetail() {
        return FraudDetail.builder()
                .browserInfo(BrowserInfo.builder()
                        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                        .acceptHeader("application/json")
                        .language("en_US")
                        .colorDepth(0)
                        .screenHeight(0)
                        .screenWidth(0)
                        .timeZoneOffset(0)
                        .javaEnabled(true)
                        .build())
                .itemCount(0)
                .totalItems(0)
                .customerIp("string")
                .deviceFingerPrint("string")
                .couponCode("")
                .build();
    }

    public static List<CommerceItem> buildCommerceItems(AuthorizationRequestInfo authorizationRequestInfo) {
        return List.of(CommerceItem.builder()
                .quantity(2)
                .commerceItemId("ci151380666")
                .salePrice("39.95")
                .displayName("AE Ne(x)t Level Curvy Jegging")
                .productUPC("429_4438")
                .sku("0033290354")
                .recipientName(null)
                .giftMessage(null)
                .recipientEmail(null)
                .recipientMobile(null)
                .build());
    }

    public static AltPayDetails buildAltPayDetails(AuthorizationRequestInfo authorizationRequestInfo) {
        return AltPayDetails.builder()
                .paypalToken(authorizationRequestInfo.getPaypalToken())
                .build();
    }

    private static double getGiftCardTotal(Double giftCardTotal, AuthorizationRequestInfo authorizationRequestInfo) {
        return StringUtils.isNotBlank(authorizationRequestInfo.getAmount()) ? Double.parseDouble(authorizationRequestInfo.getAmount()) : giftCardTotal;
    }

    private static double getTotalSum(Double totalSum, AuthorizationRequestInfo authorizationRequestInfo) {
        return StringUtils.isNotBlank(authorizationRequestInfo.getAmount()) ? Double.parseDouble(authorizationRequestInfo.getAmount()) : totalSum;
    }

}
