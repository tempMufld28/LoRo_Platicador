package com.lockchat.app.data.transport;

import com.lockchat.app.data.repository.ContactoRepositoryImpl;
import com.lockchat.app.data.repository.MensajeRepositoryImpl;
import com.lockchat.app.data.transport.ble.BleTransport;
import com.lockchat.app.data.transport.lora.LoRaUsbTransport;
import com.lockchat.app.domain.repository.IdentityRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class TransportManager_Factory implements Factory<TransportManager> {
  private final Provider<BleTransport> bleTransportProvider;

  private final Provider<LoRaUsbTransport> loRaUsbTransportProvider;

  private final Provider<IdentityRepository> identityRepositoryProvider;

  private final Provider<MensajeRepositoryImpl> mensajeRepositoryProvider;

  private final Provider<ContactoRepositoryImpl> contactoRepositoryProvider;

  public TransportManager_Factory(Provider<BleTransport> bleTransportProvider,
      Provider<LoRaUsbTransport> loRaUsbTransportProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<MensajeRepositoryImpl> mensajeRepositoryProvider,
      Provider<ContactoRepositoryImpl> contactoRepositoryProvider) {
    this.bleTransportProvider = bleTransportProvider;
    this.loRaUsbTransportProvider = loRaUsbTransportProvider;
    this.identityRepositoryProvider = identityRepositoryProvider;
    this.mensajeRepositoryProvider = mensajeRepositoryProvider;
    this.contactoRepositoryProvider = contactoRepositoryProvider;
  }

  @Override
  public TransportManager get() {
    return newInstance(bleTransportProvider.get(), loRaUsbTransportProvider.get(), identityRepositoryProvider.get(), mensajeRepositoryProvider.get(), contactoRepositoryProvider.get());
  }

  public static TransportManager_Factory create(Provider<BleTransport> bleTransportProvider,
      Provider<LoRaUsbTransport> loRaUsbTransportProvider,
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<MensajeRepositoryImpl> mensajeRepositoryProvider,
      Provider<ContactoRepositoryImpl> contactoRepositoryProvider) {
    return new TransportManager_Factory(bleTransportProvider, loRaUsbTransportProvider, identityRepositoryProvider, mensajeRepositoryProvider, contactoRepositoryProvider);
  }

  public static TransportManager newInstance(BleTransport bleTransport,
      LoRaUsbTransport loRaUsbTransport, IdentityRepository identityRepository,
      MensajeRepositoryImpl mensajeRepository, ContactoRepositoryImpl contactoRepository) {
    return new TransportManager(bleTransport, loRaUsbTransport, identityRepository, mensajeRepository, contactoRepository);
  }
}
