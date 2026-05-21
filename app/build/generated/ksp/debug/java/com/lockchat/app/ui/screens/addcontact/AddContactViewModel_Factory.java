package com.lockchat.app.ui.screens.addcontact;

import com.lockchat.app.domain.repository.ContactRepository;
import com.lockchat.app.domain.repository.IdentityRepository;
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
public final class AddContactViewModel_Factory implements Factory<AddContactViewModel> {
  private final Provider<ContactRepository> contactRepositoryProvider;

  private final Provider<IdentityRepository> identityRepositoryProvider;

  public AddContactViewModel_Factory(Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider) {
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.identityRepositoryProvider = identityRepositoryProvider;
  }

  @Override
  public AddContactViewModel get() {
    return newInstance(contactRepositoryProvider.get(), identityRepositoryProvider.get());
  }

  public static AddContactViewModel_Factory create(
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<IdentityRepository> identityRepositoryProvider) {
    return new AddContactViewModel_Factory(contactRepositoryProvider, identityRepositoryProvider);
  }

  public static AddContactViewModel newInstance(ContactRepository contactRepository,
      IdentityRepository identityRepository) {
    return new AddContactViewModel(contactRepository, identityRepository);
  }
}
