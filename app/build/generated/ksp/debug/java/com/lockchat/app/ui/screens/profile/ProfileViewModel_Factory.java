package com.lockchat.app.ui.screens.profile;

import com.lockchat.app.data.local.ThemePreferences;
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

  private final Provider<ThemePreferences> themePreferencesProvider;

  public ProfileViewModel_Factory(Provider<IdentityRepository> identityRepositoryProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<ThemePreferences> themePreferencesProvider) {
    this.identityRepositoryProvider = identityRepositoryProvider;
    this.contactRepositoryProvider = contactRepositoryProvider;
    this.themePreferencesProvider = themePreferencesProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(identityRepositoryProvider.get(), contactRepositoryProvider.get(), themePreferencesProvider.get());
  }

  public static ProfileViewModel_Factory create(
      Provider<IdentityRepository> identityRepositoryProvider,
      Provider<ContactRepository> contactRepositoryProvider,
      Provider<ThemePreferences> themePreferencesProvider) {
    return new ProfileViewModel_Factory(identityRepositoryProvider, contactRepositoryProvider, themePreferencesProvider);
  }

  public static ProfileViewModel newInstance(IdentityRepository identityRepository,
      ContactRepository contactRepository, ThemePreferences themePreferences) {
    return new ProfileViewModel(identityRepository, contactRepository, themePreferences);
  }
}
