package com.lockchat.app.ui.screens.chatdetail;

import androidx.lifecycle.SavedStateHandle;
import com.lockchat.app.data.repository.MensajeRepositoryImpl;
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
public final class ChatDetailViewModel_Factory implements Factory<ChatDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<MensajeRepositoryImpl> mensajeRepositoryProvider;

  private final Provider<TransportManager> transportManagerProvider;

  public ChatDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<MensajeRepositoryImpl> mensajeRepositoryProvider,
      Provider<TransportManager> transportManagerProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.mensajeRepositoryProvider = mensajeRepositoryProvider;
    this.transportManagerProvider = transportManagerProvider;
  }

  @Override
  public ChatDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), contactRepositoryProvider.get(), mensajeRepositoryProvider.get(), transportManagerProvider.get());
  }

  public static ChatDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<MensajeRepositoryImpl> mensajeRepositoryProvider,
      Provider<TransportManager> transportManagerProvider) {
    return new ChatDetailViewModel_Factory(savedStateHandleProvider, contactRepositoryProvider, mensajeRepositoryProvider, transportManagerProvider);
  }

  public static ChatDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      ContactRepository contactRepository, MensajeRepositoryImpl mensajeRepository,
      TransportManager transportManager) {
    return new ChatDetailViewModel(savedStateHandle, contactRepository, mensajeRepository, transportManager);
  }
}
