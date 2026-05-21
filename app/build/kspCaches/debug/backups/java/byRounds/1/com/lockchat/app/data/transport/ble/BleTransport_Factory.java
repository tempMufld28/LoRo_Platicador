package com.lockchat.app.data.transport.ble;

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
public final class BleTransport_Factory implements Factory<BleTransport> {
  private final Provider<Context> contextProvider;

  public BleTransport_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public BleTransport get() {
    return newInstance(contextProvider.get());
  }

  public static BleTransport_Factory create(Provider<Context> contextProvider) {
    return new BleTransport_Factory(contextProvider);
  }

  public static BleTransport newInstance(Context context) {
    return new BleTransport(context);
  }
}
