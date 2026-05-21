package com.lockchat.app.ui.screens.chats;

import com.lockchat.app.data.local.dao.MensajeDao;
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
public final class ChatsViewModel_Factory implements Factory<ChatsViewModel> {
  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<MensajeDao> mensajeDaoProvider;

  private final Provider<TransportManager> transportManagerProvider;

  public ChatsViewModel_Factory(Provider<ContactRepository> contactRepositoryProvider,
      Provider<MensajeDao> mensajeDaoProvider,
      Provider<TransportManager> transportManagerProvider) {
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.mensajeDaoProvider = mensajeDaoProvider;
    this.transportManagerProvider = transportManagerProvider;
  }

  @Override
  public ChatsViewModel get() {
    return newInstance(contactRepositoryProvider.get(), mensajeDaoProvider.get(), transportManagerProvider.get());
  }

  public static ChatsViewModel_Factory create(Provider<ContactRepository> contactRepositoryProvider,
      Provider<MensajeDao> mensajeDaoProvider,
      Provider<TransportManager> transportManagerProvider) {
    return new ChatsViewModel_Factory(contactRepositoryProvider, mensajeDaoProvider, transportManagerProvider);
  }

  public static ChatsViewModel newInstance(ContactRepository contactRepository,
      MensajeDao mensajeDao, TransportManager transportManager) {
    return new ChatsViewModel(contactRepository, mensajeDao, transportManager);
  }
}
