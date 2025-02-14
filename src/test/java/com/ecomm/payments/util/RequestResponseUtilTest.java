package com.ecomm.payments.util;

import com.ecomm.payments.constants.PaymentsConstants;
import com.ecomm.payments.model.ATGAuthRequest;
import com.ecomm.payments.model.Details;
import com.ecomm.payments.model.OrderSummary;
import com.ecomm.payments.model.PaymentDetailsRequest;
import com.ecomm.payments.model.PaymentDetailsResponse;
import com.ecomm.payments.model.ResponseData;
import com.ecomm.payments.model.adyen.Amount;
import com.ecomm.payments.model.atg.AtgBillingAddress;
import com.ecomm.payments.model.atg.AtgPaymentMethod;
import com.ecomm.payments.model.atg.CommerceItem;
import com.ecomm.payments.model.atg.FraudDetail;
import com.ecomm.payments.model.atg.ShippingAddress;
import com.ecomm.payments.model.atg.ShippingDetail;
import com.ecomm.payments.model.error.Error;
import com.ecomm.payments.model.error.ErrorResponse;

import java.util.ArrayList;
import java.util.List;

public class RequestResponseUtilTest {

    public static String getCheckoutResponseString() {
        return """
            	{
            	"token": "002.il3hr22qtllhetnu8ahmg4an3jbm3dhldokppafo3ncimkj4",
            	"expires": "2023-11-10T13:19:12.980Z",
            	"redirectCheckoutUrl": "https://portal.sandbox.afterpay.com/us/checkout/?token=002.il3hr22qtllhetnu8ahmg4an3jbm3dhldokppafo3ncimkj4"
            } """;
    }

    public static ATGAuthRequest getAfterpayAuthRequest() {

        ATGAuthRequest atgAuthRequest = new ATGAuthRequest();
        atgAuthRequest.setSiteId("AEO_US");
        atgAuthRequest.setCheckoutType("EXPRESS");
        atgAuthRequest.setPaymentGroupId("0032433433");
        atgAuthRequest.setAtgProfileId("3242323523532");
        atgAuthRequest.setOrderNumber("3242323523532");
        atgAuthRequest.setIdempotencyKey("UID-112345-67890");

        Amount amount = new Amount();
        amount.setValue("50.25");
        amount.setCurrency("USD");
        atgAuthRequest.setAmount(amount);

        AtgPaymentMethod paymentMethod = new AtgPaymentMethod();
        paymentMethod.setType("afterpay");
        atgAuthRequest.setPaymentMethod(paymentMethod);

        AtgBillingAddress billingAddress = new AtgBillingAddress();
        billingAddress.setAddress1("22");
        billingAddress.setAddress2("Hot Metal st");
        billingAddress.setFirstName("Test");
        billingAddress.setLastName("User");
        billingAddress.setPhoneNumber("+14005885236");
        billingAddress.setPostalCode("15220");
        billingAddress.setState("PA");
        billingAddress.setCity("Pittsburgh");
        billingAddress.setCountry("US");
        atgAuthRequest.setBillingAddress(billingAddress);

        atgAuthRequest.setShopperInteraction(PaymentsConstants.ADYEN_SHOPPER_INTERACTION_ECOMMERCE);
        atgAuthRequest.setReturnUrl("https://sit.aezone.com/us/en/checkout");

        var details = new FraudDetail();
        details.setOrderDiscountAmount("7.25");
        details.setCouponCode("10% Discount");

        var commerceItem = new CommerceItem();
        commerceItem.setDisplayName("AE Super Soft Eagle Graphic T-Shirt");
        commerceItem.setSku("0035708643");
        commerceItem.setQuantity("1");
        commerceItem.setSalePrice("57.25");
        details.setCommerceItems(List.of(commerceItem));

        var shippingDetail = new ShippingDetail();
        shippingDetail.setShippingAmount(7.95);

        var shippingAddress = new ShippingAddress();
        shippingAddress.setFirstName("Samanth");
        shippingAddress.setLastName("Meesala");
        shippingAddress.setAddress1("19 dickerson dr");
        shippingAddress.setAddress2("2nd street");
        shippingAddress.setCity("Piscataway");
        shippingAddress.setState("NJ");
        shippingAddress.setPostalCode("08854");
        shippingDetail.setShippingAddress(shippingAddress);
        shippingDetail.setShippingMethod("STD");

        details.setShippingDetail(shippingDetail);
        atgAuthRequest.setAdyenFraudDetail(details);
        return atgAuthRequest;
    }

    public static PaymentDetailsRequest getAuthDetailsRequest() {
        var authDetailsRequest = new PaymentDetailsRequest();
        Details details = new Details();
        details.setAfterpayToken("002.71g26rn67eg5c84gcgtiaiq62u6hi579ckujog7jjff0i501");
        OrderSummary orderSummary = new OrderSummary();
        orderSummary.setChannelType("WEB");
        orderSummary.setCheckoutType("EXPRESS");
        orderSummary.setCurrency("USD");
        orderSummary.setOrderTotal(50.00);
        orderSummary.setAfterpayInstallment(10.99);
        authDetailsRequest.setOrderSummary(orderSummary);
        authDetailsRequest.setDetails(details);
        authDetailsRequest.setOrderNumber("00012345678");
        return authDetailsRequest;
    }

    public static String getAfterpayAuthResponseString() {
        return """
            					{
              "id" : "100203444519",
              "token" : "002.em5vos7eumu87po1a7sstik3le2rrdhipspg9i8hak9ba756",
              "status" : "APPROVED",
              "created" : "2023-12-07T13:55:49.208Z",
              "originalAmount" : {
                "amount" : "74.93",
                "currency" : "USD"
              },
              "openToCaptureAmount" : {
                "amount" : "74.93",
                "currency" : "USD"
              },
              "paymentState" : "AUTH_APPROVED",
              "merchantReference" : "000326025",
              "refunds" : [ ],
              "orderDetails" : {
                "consumer" : {
                  "phoneNumber" : "+1656456123",
                  "givenNames" : "Samanth",
                  "surname" : "Meesala",
                  "email" : "aeo_USA_afterpay@example.com"
                },
                "billing" : {
                  "name" : "Volodymyr Test",
                  "line1" : "Hot Metal Street Bridge",
                  "area1" : "Pittsburgh",
                  "region" : "PA",
                  "postcode" : "15203",
                  "countryCode" : "US",
                  "phoneNumber" : "+16567898765"
                },
                "shipping" : {
                  "name" : "Mark Raj",
                  "line1" : "Hot Metal Street Bridge",
                  "area1" : "Pittsburgh",
                  "region" : "PA",
                  "postcode" : "15203",
                  "countryCode" : "US"
                },
                "courier" : {
                  "priority" : "STANDARD"
                },
                "items" : [ {
                  "name" : "AE AirFlex 360 Skinny Jean",
                  "sku" : "0035448345",
                  "quantity" : 1,
                  "price" : {
                    "amount" : "74.93",
                    "currency" : "USD"
                  }
                } ],
                "discounts":[
                  {
                     "displayName":"promo200001,1894VCAF27B9WG6JM38G,promo290029,promo950001,promo940004,promo1640011,promo380008,1U62UVAE9MA8PA9SV94W,1M99WUAF2RJ3DG6VE6PS",
                     "amount":{
                        "amount":"26.48",
                        "currency":"USD"
                     }
                  }
               ],
                "shippingAmount" : {
                  "amount" : "0.00",
                  "currency" : "USD"
                },
                "taxAmount" : {
                  "amount" : "0.00",
                  "currency" : "USD"
                }
              },
              "events" : [ {
                "id" : "2ZDXLYkDejYZknHpQgXaXNSkQn5",
                "created" : "2023-12-07T13:59:27.668Z",
                "expires" : "2023-12-14T13:59:27.667Z",
                "type" : "AUTH_APPROVED",
                "amount" : {
                  "amount" : "74.93",
                  "currency" : "USD"
                },
                "paymentEventMerchantReference" : null
              } ]
            } """;
    }

    public static String getAfterpayAuthResponseString1() {
        return """
            					{
              "id" : "100203444519",
              "token" : "002.em5vos7eumu87po1a7sstik3le2rrdhipspg9i8hak9ba756",
              "status" : "APPROVED",
              "created" : "2023-12-07T13:55:49.208Z",
              "originalAmount" : {
                "amount" : null,
                "currency" : "USD"
              },
              "openToCaptureAmount" : {
                "amount" : "74.93",
                "currency" : "USD"
              },
              "paymentState" : "AUTH_APPROVED",
              "merchantReference" : "000326025",
              "refunds" : [ ],
              "orderDetails" : null,
              "events" : null
            } """;
    }

    public static String getAfterpayAuthResponseString2() {
        return """
            					{
              "id" : "100203444519",
              "token" : "002.em5vos7eumu87po1a7sstik3le2rrdhipspg9i8hak9ba756",
              "status" : "APPROVED",
              "created" : "2023-12-07T13:55:49.208Z",
              "originalAmount" : {
                "amount" : null,
                "currency" : "USD"
              },
              "openToCaptureAmount" : {
                "amount" : "74.93",
                "currency" : "USD"
              },
              "paymentState" : "AUTH_APPROVED",
              "merchantReference" : "000326025",
              "refunds" : [ ],
              "orderDetails" : {
                "consumer" : {
                  "phoneNumber" : "+16567898765",
                  "givenNames" : "Samanth",
                  "surname" : "Meesala",
                  "email" : "aeo_USA_afterpay@example.com"
                },
                "billing" : null,
                "shipping" : {
                  "name" : "Volodymyr Test",
                  "line1" : "Hot Metal Street Bridge",
                  "area1" : "Pittsburgh",
                  "region" : "PA",
                  "postcode" : "15203",
                  "countryCode" : "US",
                  "phoneNumber" : "+16567898765"
                },
                "courier" : {
                  "priority" : "STANDARD"
                },
                "items" : [ {
                  "name" : "AE AirFlex 360 Skinny Jean",
                  "sku" : "0035448345",
                  "quantity" : 1,
                  "price" : {
                    "amount" : "74.93",
                    "currency" : "USD"
                  }
                } ],
                "discounts" : [ ],
                "shippingAmount" : {
                  "amount" : "0.00",
                  "currency" : "USD"
                },
                "taxAmount" : {
                  "amount" : "0.00",
                  "currency" : "USD"
                }
              },
              "events" : [ ]
            } """;
    }

    public static String getAfterpayAuthResponseStringWithoutAmount() {
        return """
            					{
              "id" : "100203444519",
              "token" : "002.em5vos7eumu87po1a7sstik3le2rrdhipspg9i8hak9ba756",
              "status" : "APPROVED",
              "created" : "2023-12-07T13:55:49.208Z",
              "originalAmount" : {
                "amount" : "74.93",
                "currency" : "USD"
              },
              "openToCaptureAmount" : {
                "amount" : "74.93",
                "currency" : "USD"
              },
              "paymentState" : "AUTH_APPROVED",
              "merchantReference" : "000326025",
              "refunds" : [ ],
              "orderDetails" : {
                "consumer" : {
                  "phoneNumber" : "+16567898765",
                  "givenNames" : "Samanth",
                  "surname" : "Meesala",
                  "email" : "aeo_USA_afterpay@example.com"
                },
                "billing" : {
                  "name" : "Volodymyr Test",
                  "line1" : "Hot Metal Street Bridge",
                  "area1" : "Pittsburgh",
                  "region" : "PA",
                  "postcode" : "15203",
                  "countryCode" : "US",
                  "phoneNumber" : "+16567898765"
                },
                "shipping" : {
                  "name" : "Volodymyr Test",
                  "line1" : "Hot Metal Street Bridge",
                  "area1" : "Pittsburgh",
                  "region" : "PA",
                  "postcode" : "15203",
                  "countryCode" : "US",
                  "phoneNumber" : "+16567898765"
                },
                "courier" : {
                  "priority" : "STANDARD"
                },
                "items" : [ {
                  "name" : "AE AirFlex 360 Skinny Jean",
                  "sku" : "0035448345",
                  "quantity" : 1,
                  "price" : {
                    "amount" : "74.93",
                    "currency" : "USD"
                  }
                } ],
                "discounts" : [ ],
                "shippingAmount" : {
                  "amount" : "0.00",
                  "currency" : "USD"
                },
                "taxAmount" : {
                  "amount" : "0.00",
                  "currency" : "USD"
                }
              },
              "events" : [ {
                "id" : "2ZDXLYkDejYZknHpQgXaXNSkQn5",
                "created" : "2023-12-07T13:59:27.668Z",
                "expires" : "2023-12-14T13:59:27.667Z",
                "type" : "AUTH_APPROVED",
                "amount" : null,
                "paymentEventMerchantReference" : null
              } ]
            } """;
    }

    public static String getAfterpayAuthErrorResponseString() {
        return """
            					{
              "errorCode" : "invalid_order_transaction_status",
              "errorId" : "9608406bbd839bbf",
              "message" : "Order has not been approved.",
              "httpStatusCode" : 412

            } """;
    }

    public static PaymentDetailsResponse getAuthDetailsResponse() {
        var response = new PaymentDetailsResponse();
        var errorResponse = new ErrorResponse();
        List<Error> errors = new ArrayList<>();
        errors.add(new Error(ErrorType.AUTH_FAILED, new String[] { PaymentsConstants.PAYMENT_FIELD }));
        errorResponse.setErrors(errors);
        response.setError(errorResponse);
        response.setData(new ResponseData());
        return response;
    }

}
