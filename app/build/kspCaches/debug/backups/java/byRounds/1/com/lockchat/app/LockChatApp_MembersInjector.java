package com.lockchat.app;

import androidx.hilt.work.HiltWorkerFactory;
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
public final class LockChatApp_MembersInjector implements MembersInjector<LockChatApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public LockChatApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<LockChatApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new LockChatApp_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(LockChatApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.lockchat.app.LockChatApp.workerFactory")
  public static void injectWorkerFactory(LockChatApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
