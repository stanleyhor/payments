package com.ecomm.payments.util;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.atg.CommerceItem;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class AdditionalDetailsMapper {

    public Map<String, String> getAdditionalDetails(ATGAuthRequest request) {
        Map<String, String> additionalData = new HashMap<>();

        if (request.getAdditionalData() != null) {
            additionalData.put(PaymentsConstants.REQUESTED_TEST_ACQUIRER_RC, request.getAdditionalData()
                    .getRequestedTestAcquirerResponseCode());
        }
        if (request.getAdyenFraudDetail() != null) {

            if (request.getAdyenFraudDetail()
                    .getShippingDetail() != null) {
                additionalData.put(PaymentsConstants.RISK_SHIPPING_METHOD, request.getAdyenFraudDetail()
                        .getShippingDetail()
                        .getShippingMethod());
                populateNeighborhood(request, additionalData);
            }

            populateBasketItem(request, additionalData);
            populateAdditionalFraudFields(request, additionalData);

            if (request.getPaymentMethod() != null
                    && !PaymentsConstants.CREDIT_CARD.equalsIgnoreCase(request.getPaymentMethod()
                            .getType())) {
                additionalData.put(PaymentsConstants.ALTERNATE_PAYMENT_TYPE, String.valueOf(request.getPaymentMethod()
                        .getType()));
            }
            // Added for 3DS
            additionalData.put(PaymentsConstants.EXECUTE_3D, request.getExecuteThreeD());

        }

        // Added for synchrony
        if (request.getEnhancedSchemeData() != null) {

            additionalData.put(PaymentsConstants.ENHANCEDSCHEMEDATA_CUSTOMER_REFERENCE, request.getEnhancedSchemeData()
                    .getCustomerReference());
            additionalData.put(PaymentsConstants.ENHANCEDSCHEMEDATA_TOTAL_TAXAMOUNT, String.valueOf(request.getEnhancedSchemeData()
                    .getTotalTaxAmount()));
            populateItemDetailLineList(request, additionalData);
        }

        additionalData.values()
                .removeIf(Objects::isNull);
        additionalData.values()
                .removeIf(x -> "null".equalsIgnoreCase(x));

        return additionalData;
    }

    private static void populateAdditionalFraudFields(ATGAuthRequest request, Map<String, String> additionalData) {

        var adyenFraudDetail = request.getAdyenFraudDetail();
        String couponCode = adyenFraudDetail.getCouponCode();

        // Added fraud fields for credit card MX site
        additionalData.put(PaymentsConstants.RISK_CHANNEL, request.getRequestContext()
                .getChannelType());
        additionalData.put(PaymentsConstants.RISK_LOYALTY, adyenFraudDetail.isHasLoyalty() ? PaymentsConstants.YES : PaymentsConstants.NO);

        if (request.getAdyenFraudDetail()
                .getCommerceItems() != null) {
            additionalData.put(PaymentsConstants.RISK_CART_TYPE, getCartType(request.getAdyenFraudDetail()
                    .getCommerceItems()));
            additionalData.put(PaymentsConstants.RISK_LINE_ITEMS, adyenFraudDetail.getCommerceItemCount());
            additionalData.put(PaymentsConstants.RISK_TOTAL_ITEMS, String.valueOf(adyenFraudDetail.getTotalItems()));
            additionalData.put(PaymentsConstants.TIME_ON_FILE, String.valueOf(adyenFraudDetail.getDaysSinceRegistration()));

        }

        // Need to check - risk
        additionalData.put(PaymentsConstants.PROMOTION_CODE, couponCode);
        additionalData.put(PaymentsConstants.RISK_ORDER_DISCOUNT, adyenFraudDetail.getOrderDiscountAmount());

        if (Objects.nonNull(couponCode)) {
            additionalData.put(PaymentsConstants.RISK_COUPON_QUANTITY, String.valueOf(getPromoCouponCount(couponCode)));
        }

        if (adyenFraudDetail.getShippingDetail() != null
                && PaymentsConstants.SHIPPING_METHOD_BOPIS.equalsIgnoreCase(adyenFraudDetail.getShippingDetail()
                        .getShippingMethod())
                && adyenFraudDetail.getShippingDetail()
                        .getShippingAddress() != null
                && adyenFraudDetail.getContactInfo() != null) {

            var shippingAddress = adyenFraudDetail.getShippingDetail()
                    .getShippingAddress();

            additionalData.put(PaymentsConstants.RISK_PICKUP_PERSON_EMAIL, adyenFraudDetail.getContactInfo()
                    .getEmail());
            additionalData.put(PaymentsConstants.RISK_PICKUP_PERSON_PHONE, adyenFraudDetail.getContactInfo()
                    .getPhoneNo());
            additionalData.put(PaymentsConstants.RISK_PICKUP_PERSON_FIRSTNAME, shippingAddress.getFirstName());
            additionalData.put(PaymentsConstants.RISK_PICKUP_PERSON_LASTNAME, shippingAddress.getLastName());
            additionalData.put(PaymentsConstants.RISK_PICKUP_STREET_ADDRESS, shippingAddress.getAddress1());
            additionalData.put(PaymentsConstants.RISK_PICKUP_CITY, shippingAddress.getCity());
            additionalData.put(PaymentsConstants.RISK_PICKUP_STATE, shippingAddress.getState());
            additionalData.put(PaymentsConstants.RISK_PICKUP_ZIPCODE, shippingAddress.getPostalCode());
            additionalData.put(PaymentsConstants.RISK_PICKUP_COUNTRY, shippingAddress.getCountry());

        }
    }

    private static String getCartType(List<CommerceItem> commerceItems) {
        return commerceItems.stream()
                .map(CommerceItem::getItemType)
                .filter(Objects::nonNull)
                .map(String::toUpperCase)
                .filter(type -> Arrays.stream(CartType.values())
                        .anyMatch(itemtype -> itemtype.type.equals(type)))
                .distinct()
                .collect(Collectors.joining(PaymentsConstants.COMMA));
    }

    private static long getPromoCouponCount(String couponCode) {
        return Arrays.stream(couponCode.split(PaymentsConstants.COUPON_CODE_SEPARATOR))
                .filter(coupon -> (!coupon.isBlank()
                        && !coupon.toUpperCase()
                                .startsWith(PaymentsConstants.PROMO)))
                .count();
    }

    private void populateBasketItem(ATGAuthRequest request, Map<String, String> additionalData) {
        if (request.getAdyenFraudDetail()
                .getCommerceItems() != null) {
            List<CommerceItem> commerceItems = request.getAdyenFraudDetail()
                    .getCommerceItems();
            for (int i = 1; i <= commerceItems.size(); i++) {
                additionalData.put(PaymentsConstants.RISK_BASKET_ITEM
                        + i
                        + "."
                        + "quantity",
                        commerceItems.get(i - 1)
                                .getQuantity());
                additionalData.put(PaymentsConstants.RISK_BASKET_ITEM
                        + i
                        + ".amountPerItem",
                        commerceItems.get(i - 1)
                                .getSalePrice());
                additionalData.put(PaymentsConstants.RISK_BASKET_ITEM
                        + i
                        + ".productTitle",
                        commerceItems.get(i - 1)
                                .getDisplayName());
                additionalData.put(PaymentsConstants.RISK_BASKET_ITEM
                        + i
                        + ".itemID",
                        commerceItems.get(i - 1)
                                .getProductUPC());
                additionalData.put(PaymentsConstants.RISK_BASKET_ITEM
                        + i
                        + ".sku",
                        commerceItems.get(i - 1)
                                .getSku());
                // Added code for PAYMS-35
                if (StringUtils.isNotBlank(commerceItems.get(i - 1)
                        .getRecipientName())) {
                    additionalData.put(PaymentsConstants.RISK_BASKET_ITEM
                            + i
                            + ".brand",
                            commerceItems.get(i - 1)
                                    .getRecipientName());
                }
                if (StringUtils.isNotBlank(commerceItems.get(i - 1)
                        .getRecipientEmail())) {
                    additionalData.put(PaymentsConstants.RISK_BASKET_ITEM
                            + i
                            + ".receiverEmail",
                            commerceItems.get(i - 1)
                                    .getRecipientEmail());
                }
                if (StringUtils.isNotBlank(commerceItems.get(i - 1)
                        .getRecipientMobile())) {
                    additionalData.put(PaymentsConstants.RISK_BASKET_ITEM
                            + i
                            + ".color",
                            commerceItems.get(i - 1)
                                    .getRecipientMobile());
                }
                if (StringUtils.isNotBlank(commerceItems.get(i - 1)
                        .getGiftMessage())) {
                    additionalData.put(PaymentsConstants.RISK_BASKET_ITEM
                            + i
                            + ".upc",
                            commerceItems.get(i - 1)
                                    .getGiftMessage());
                }
            }
        }
    }

    private void populateNeighborhood(ATGAuthRequest request, Map<String, String> additionalData) {
        if (request.getAdyenFraudDetail()
                .getShippingDetail()
                .getShippingAddress() != null
                && StringUtils.isNotBlank(request.getAdyenFraudDetail()
                        .getShippingDetail()
                        .getShippingAddress()
                        .getNeighborhood())) {
            additionalData.put(PaymentsConstants.RISK_SHIPPING_ADDRESS_NEIGHBORHOOD, request.getAdyenFraudDetail()
                    .getShippingDetail()
                    .getShippingAddress()
                    .getNeighborhood());
        }
    }

    /**
     * @param request
     * @param additionalData
     */
    private void populateItemDetailLineList(ATGAuthRequest request, Map<String, String> additionalData) {

        if (request.getEnhancedSchemeData()
                .getItemDetailLineList() != null) {
            var detailLineList = request.getEnhancedSchemeData()
                    .getItemDetailLineList();
            for (int i = 1; i <= detailLineList.size(); i++) {
                additionalData.put(PaymentsConstants.ENHANCEDSCHEMEDATA_ITEMDETAILLINE + i + PaymentsConstants.DOT + PaymentsConstants.PRODUCT_CODE,
                        detailLineList.get(i - 1)
                                .getProductCode());
                additionalData.put(PaymentsConstants.ENHANCEDSCHEMEDATA_ITEMDETAILLINE + i + PaymentsConstants.DOT + PaymentsConstants.COMMODITY_CODE,
                        detailLineList.get(i - 1)
                                .getCommodityCode());
                additionalData.put(PaymentsConstants.ENHANCEDSCHEMEDATA_ITEMDETAILLINE + i + PaymentsConstants.DOT + PaymentsConstants.DESCRIPTION,
                        StringUtils.truncate(removeNonASCII(detailLineList.get(i - 1)
                                .getDescription()), 26));
                additionalData.put(PaymentsConstants.ENHANCEDSCHEMEDATA_ITEMDETAILLINE + i + PaymentsConstants.DOT + PaymentsConstants.QUANTITY,
                        String.valueOf(detailLineList.get(i - 1)
                                .getQuantity()));
                additionalData.put(PaymentsConstants.ENHANCEDSCHEMEDATA_ITEMDETAILLINE + i + PaymentsConstants.DOT + PaymentsConstants.UNIT_OF_MEASURE,
                        detailLineList.get(i - 1)
                                .getUnitOfMeasure());
                additionalData.put(PaymentsConstants.ENHANCEDSCHEMEDATA_ITEMDETAILLINE + i + PaymentsConstants.DOT + PaymentsConstants.UNIT_PRICE,
                        String.valueOf(detailLineList.get(i - 1)
                                .getUnitPrice()));
                additionalData.put(PaymentsConstants.ENHANCEDSCHEMEDATA_ITEMDETAILLINE + i + PaymentsConstants.DOT + PaymentsConstants.TOTAL_AMOUNT,
                        String.valueOf(detailLineList.get(i - 1)
                                .getTotalAmount()));
                additionalData.put(PaymentsConstants.ENHANCEDSCHEMEDATA_ITEMDETAILLINE + i + PaymentsConstants.DOT + PaymentsConstants.DISCOUNT_AMOUNT,
                        String.valueOf(detailLineList.get(i - 1)
                                .getDiscountAmount()));
            }
        }
    }

    private String removeNonASCII(String input) {
        if (ObjectUtils.isEmpty(input)) {
            return input;
        }
        return input.replaceAll("[^\\p{ASCII}]", "");
    }

}
