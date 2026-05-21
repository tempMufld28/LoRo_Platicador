package com.lockchat.app.ui.screens.onboarding;

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
public final class OnboardingViewModel_Factory implements Factory<OnboardingViewModel> {
  private final Provider<IdentityRepository> identityRepositoryProvider;

  public OnboardingViewModel_Factory(Provider<IdentityRepository> identityRepositoryProvider) {
    this.identityRepositoryProvider = identityRepositoryProvider;
  }

  @Override
  public OnboardingViewModel get() {
    return newInstance(identityRepositoryProvider.get());
  }

  public static OnboardingViewModel_Factory create(
      Provider<IdentityRepository> identityRepositoryProvider) {
    return new OnboardingViewModel_Factory(identityRepositoryProvider);
  }

  public static OnboardingViewModel newInstance(IdentityRepository identityRepository) {
    return new OnboardingViewModel(identityRepository);
  }
}
