package com.lockchat.app.transport;

import com.lockchat.app.data.transport.TransportManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@QualifierMetadata
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
public final class UsbEventReceiver_MembersInjector implements MembersInjector<UsbEventReceiver> {
  private final Provider<TransportManager> transportManagerProvider;

  public UsbEventReceiver_MembersInjector(Provider<TransportManager> transportManagerProvider) {
    this.transportManagerProvider = transportManagerProvider;
  }

  public static MembersInjector<UsbEventReceiver> create(
      Provider<TransportManager> transportManagerProvider) {
    return new UsbEventReceiver_MembersInjector(transportManagerProvider);
  }

  @Override
  public void injectMembers(UsbEventReceiver instance) {
    injectTransportManager(instance, transportManagerProvider.get());
  }

  @InjectedFieldSignature("com.lockchat.app.transport.UsbEventReceiver.transportManager")
  public static void injectTransportManager(UsbEventReceiver instance,
      TransportManager transportManager) {
    instance.transportManager = transportManager;
  }
}
