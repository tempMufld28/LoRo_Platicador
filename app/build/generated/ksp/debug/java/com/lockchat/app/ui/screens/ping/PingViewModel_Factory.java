package com.lockchat.app.ui.screens.ping;

import com.lockchat.app.data.transport.TransportManager;
import com.lockchat.app.domain.repository.ContactRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class PingViewModel_Factory implements Factory<PingViewModel> {
  private final Provider<TransportManager> transportManagerProvider;

  private final Provider<ContactRepository> contactRepositoryProvider;

  public PingViewModel_Factory(Provider<TransportManager> transportManagerProvider,
      Provider<ContactRepository> contactRepositoryProvider) {
    this.transportManagerProvider = transportManagerProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
  }

  @Override
  public PingViewModel get() {
    return newInstance(transportManagerProvider.get(), contactRepositoryProvider.get());
  }

  public static PingViewModel_Factory create(Provider<TransportManager> transportManagerProvider,
      Provider<ContactRepository> contactRepositoryProvider) {
    return new PingViewModel_Factory(transportManagerProvider, contactRepositoryProvider);
  }

  public static PingViewModel newInstance(TransportManager transportManager,
      ContactRepository contactRepository) {
    return new PingViewModel(transportManager, contactRepository);
  }
}
