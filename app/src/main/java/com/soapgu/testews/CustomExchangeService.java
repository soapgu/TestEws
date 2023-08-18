package com.soapgu.testews;

import android.org.apache.http.config.Registry;
import android.org.apache.http.config.RegistryBuilder;
import android.org.apache.http.conn.socket.ConnectionSocketFactory;
import android.org.apache.http.conn.socket.PlainConnectionSocketFactory;
import android.org.apache.http.conn.ssl.NoopHostnameVerifier;

import java.security.GeneralSecurityException;

import microsoft.exchange.webservices.data.EWSConstants;
import microsoft.exchange.webservices.data.core.EwsSSLProtocolSocketFactory;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;

public class CustomExchangeService extends ExchangeService {

    public CustomExchangeService(ExchangeVersion version){
        super( version );
    }

    @Override
    protected Registry<ConnectionSocketFactory> createConnectionSocketFactoryRegistry() {
        try {
            return RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(EWSConstants.HTTP_SCHEME, new PlainConnectionSocketFactory())
                    .register(EWSConstants.HTTPS_SCHEME, EwsSSLProtocolSocketFactory.build(
                            null, NoopHostnameVerifier.INSTANCE
                    ))
                    .build();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(
                    "Could not initialize ConnectionSocketFactory instances for HttpClientConnectionManager", e
            );
        }
    }

}
