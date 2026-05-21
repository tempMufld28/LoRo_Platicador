package com.lockchat.app.data.transport.lora;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class LoRaUsbTransport_Factory implements Factory<LoRaUsbTransport> {
  private final Provider<Context> contextProvider;

  public LoRaUsbTransport_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public LoRaUsbTransport get() {
    return newInstance(contextProvider.get());
  }

  public static LoRaUsbTransport_Factory create(Provider<Context> contextProvider) {
    return new LoRaUsbTransport_Factory(contextProvider);
  }

  public static LoRaUsbTransport newInstance(Context context) {
    return new LoRaUsbTransport(context);
  }
}
