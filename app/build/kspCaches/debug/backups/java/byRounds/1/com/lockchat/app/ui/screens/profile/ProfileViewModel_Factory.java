package com.lockchat.app.ui.screens.profile;

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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<IdentityRepository> identityRepositoryProvider;

  private final Provider<ContactRepository> contactRepositoryProvider;

  public ProfileViewModel_Factory(Provider<IdentityRepository> identityRepositoryProvider,
      Provider<ContactRepository> contactRepositoryProvider) {
    this.identityRepositoryProvider = identityRepositoryProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(identityRepositoryProvider.get(), contactRepositoryProvider.get());
  }

  public static ProfileViewModel_Factory create(
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<ContactRepository> contactRepositoryProvider) {
    return new ProfileViewModel_Factory(identityRepositoryProvider, contactRepositoryProvider);
  }

  public static ProfileViewModel newInstance(IdentityRepository identityRepository,
      ContactRepository contactRepository) {
    return new ProfileViewModel(identityRepository, contactRepository);
  }
}
