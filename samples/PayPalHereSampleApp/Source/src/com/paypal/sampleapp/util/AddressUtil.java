/**
 * PayPalHereSDK
 * <p/>
 * Created by PayPal Here SDK Team.
 * Copyright (c) 2013 PayPal. All rights reserved.
 */
package com.paypal.sampleapp.util;

import com.paypal.merchant.sdk.domain.Address;
import com.paypal.merchant.sdk.domain.DomainFactory;

public class AddressUtil {

    public static Address getDefaultUSMerchantAddress() {
        Address.Builder builder = DomainFactory.newAddressBuilder();
        builder.
                setLine1("2211 N 1st street").
                setLine2("Apt #12").
                setCity("San Jose").
                setState("CA").
                setCountryCode("US").
                setPostalCode("95126").
                setPhoneNumber("4084084080");
        return builder.build();
    }

    public static Address getDefaultUKMerchantAddress() {
        Address.Builder builder = DomainFactory.newAddressBuilder();
        builder.
                setLine1("34726 South Broadway").
                setCity("Wolverhampton").
                setState("West Midlands").
                setCountryCode("GB").
                setPostalCode("W12 4LQ").
                setPhoneNumber("05725854438");
        return builder.build();
    }
}
