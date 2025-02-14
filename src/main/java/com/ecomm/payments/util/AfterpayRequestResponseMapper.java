package com.ecomm.payments.util;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.afterpay.AfterpayAddress;
import com.ecomm.payments.model.afterpay.AfterpayAmount;
import com.ecomm.payments.model.afterpay.AfterpayAuthRequest;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutRequest;
import com.ecomm.payments.model.afterpay.AfterpayCheckoutResponse;
import com.ecomm.payments.model.afterpay.Consumer;
import com.ecomm.payments.model.afterpay.Courier;
import com.ecomm.payments.model.afterpay.Discount;
import com.ecomm.payments.model.afterpay.Item;
import com.ecomm.payments.model.afterpay.Merchant;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.atg.CommerceItem;
import com.ecomm.payments.model.atg.FraudDetail;
import com.ecomm.payments.model.atg.ShippingAddress;
import com.ecomm.payments.model.atg.ShippingDetail;
import com.google.gson.Gson;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class AfterpayRequestResponseMapper {

    public AfterpayCheckoutRequest convertToCheckoutRequest(ATGAuthRequest authRequest) {

        var checkoutType = authRequest.getCheckoutType();
        var currency = authRequest.getAmount()
                .getCurrency();

        var checkoutAfterpayRequest = new AfterpayCheckoutRequest();
        checkoutAfterpayRequest.setMerchantReference(authRequest.getOrderNumber());
        checkoutAfterpayRequest.setMerchant(getMerchant(checkoutType, authRequest.getReturnUrl()));
        checkoutAfterpayRequest.setAmount(getOrderTotal(authRequest.getAmount()));

        boolean isCashAppPayment = PaymentsConstants.PAYMENT_METHOD_CASHAPP.equalsIgnoreCase(authRequest.getPaymentMethod()
                .getType());
        checkoutAfterpayRequest.setCashAppPay(isCashAppPayment);

        if (PaymentsConstants.CHECKOUT_TYPE_EXPRESS.equalsIgnoreCase(checkoutType)) {
            checkoutAfterpayRequest.setShippingOptionIdentifier(authRequest.getAfterpayShippingOptionIdentifier());
            checkoutAfterpayRequest.setMode(checkoutType);
        } else {
            checkoutAfterpayRequest.setItems(getCommerceItems(authRequest.getAdyenFraudDetail()
                    .getCommerceItems(), currency));
            checkoutAfterpayRequest.setConsumer(getConsumer(authRequest.getBillingAddress()));
            var billingAddress = getBillingAddress(authRequest);
            var shippingDetail = authRequest.getAdyenFraudDetail()
                    .getShippingDetail();
            checkoutAfterpayRequest
                    .setShipping(null != shippingDetail.getShippingAddress() ? getShippingAddress(shippingDetail.getShippingAddress()) : billingAddress);
            checkoutAfterpayRequest.setBilling(billingAddress);

            // Optional
            checkoutAfterpayRequest.setDiscounts(getDiscounts(authRequest.getAdyenFraudDetail(), currency));

            if (null != authRequest.getEnhancedSchemeData()) {
                checkoutAfterpayRequest.setTaxAmount(getTaxAmount(currency, authRequest.getEnhancedSchemeData()
                        .getTotalTaxAmount()));
            }
            checkoutAfterpayRequest.setShippingAmount(getShippingAmount(currency, shippingDetail.getShippingAmount()));
            checkoutAfterpayRequest.setCourier(getCourier(shippingDetail));

        }

        return checkoutAfterpayRequest;
    }

    private AfterpayAmount getOrderTotal(Amount amount) {

        return getPrice(amount.getCurrency(), String.valueOf(CommonUtils.convertCentsInToDollars(amount.getValue())));

    }

    private Consumer getConsumer(AtgBillingAddress billingAddress) {

        return Consumer.builder()
                .givenNames(billingAddress.getFirstName())
                .surname(billingAddress.getLastName())
                .email(billingAddress.getEmail())
                .phoneNumber(billingAddress.getPhoneNumber())
                .build();

    }

    private AfterpayAddress getShippingAddress(ShippingAddress shippingAddress) {

        return AfterpayAddress.builder()
                .name(getFullName(shippingAddress.getFirstName(), shippingAddress.getLastName()))
                .line1(shippingAddress.getAddress1())
                .line2(shippingAddress.getAddress2())
                .area1(shippingAddress.getCity())
                .region(shippingAddress.getState())
                .postcode(shippingAddress.getPostalCode())
                .countryCode(shippingAddress.getCountry())
                .build();

    }

    private AfterpayAddress getBillingAddress(ATGAuthRequest authRequest) {

        var billingAddress = authRequest.getBillingAddress();

        return AfterpayAddress.builder()
                .name(getFullName(billingAddress.getFirstName(), billingAddress.getLastName()))
                .line1(billingAddress.getAddress1())
                .line2(billingAddress.getAddress2())
                .area1(billingAddress.getCity())
                .region(billingAddress.getState())
                .postcode(billingAddress.getPostalCode())
                .countryCode(billingAddress.getCountry())
                .phoneNumber(billingAddress.getPhoneNumber())
                .build();

    }

    private String getFullName(String firstName, String lastName) {

        return new StringBuilder().append(firstName)
                .append(PaymentsConstants.SINGLE_SPACE)
                .append(lastName)
                .toString();

    }

    private List<Item> getCommerceItems(List<CommerceItem> commerceItems, String currencyCode) {
        List<Item> items = new ArrayList<>();
        for (CommerceItem commerceItem : commerceItems) {
            items.add(Item.builder()
                    .sku(commerceItem.getSku())
                    .quantity(Long.parseLong(commerceItem.getQuantity()))
                    .price(getPrice(currencyCode, commerceItem.getSalePrice()))
                    .name(commerceItem.getDisplayName())
                    .build());
        }
        return items;
    }

    private List<Discount> getDiscounts(FraudDetail fraudDetail, String currency) {

        List<Discount> discounts = new ArrayList<>();

        String couponCode = fraudDetail.getCouponCode();

        if (StringUtils.hasText(couponCode)) {

            if (couponCode.length() > 128) {
                couponCode = couponCode.substring(0, 127);
            }

            discounts.add(Discount.builder()
                    .displayName(couponCode)
                    .amount(getPrice(currency, fraudDetail.getOrderDiscountAmount()))
                    .build());
        }

        return discounts;
    }

    private Merchant getMerchant(String checkoutType, String redirectUrl) {

        var merchant = new Merchant();

        if (PaymentsConstants.CHECKOUT_TYPE_EXPRESS.equalsIgnoreCase(checkoutType)) {
            merchant.setPopupOriginUrl(redirectUrl);
        } else {
            merchant.setRedirectConfirmUrl(redirectUrl);
            merchant.setRedirectCancelUrl(redirectUrl);
        }

        return merchant;
    }

    private AfterpayAmount getTaxAmount(String currency, long taxTotal) {

        return getPrice(currency, String.valueOf(taxTotal));

    }

    private AfterpayAmount getShippingAmount(String currency, double shippingTotal) {

        return getPrice(currency, String.valueOf(shippingTotal));

    }

    private Courier getCourier(ShippingDetail shippingDetail) {

        return Courier.builder()
                .priority(PaymentsConstants.SHIP_CODE_STD
                        .equalsIgnoreCase(shippingDetail.getShippingMethod()) ? PaymentsConstants.STANDARD : PaymentsConstants.EXPRESS)
                .build();

    }

    private AfterpayAmount getPrice(String currency, String amount) {

        return AfterpayAmount.builder()
                .amount(amount)
                .currency(currency)
                .build();

    }

    public AfterpayCheckoutResponse buildCheckoutResponse(String stringResponse, String orderNumber) {

        var afterPayCheckoutResponse = new Gson().fromJson(stringResponse, AfterpayCheckoutResponse.class);
        afterPayCheckoutResponse.setOrderNumber(orderNumber);
        return afterPayCheckoutResponse;

    }

    public AfterpayAuthRequest convertToAuthorizeRequest(PaymentDetailsRequest detailsRequest) {

        return AfterpayAuthRequest.builder()
                .requestId(UUID.randomUUID()
                        .toString())
                .token(detailsRequest.getDetails()
                        .getAfterpayToken())
                .merchantReference(detailsRequest.getOrderNumber())
                .amount(getPrice(detailsRequest.getOrderSummary()
                        .getCurrency(),
                        String.valueOf(detailsRequest.getOrderSummary()
                                .getOrderTotal())))
                .build();
    }

}
