package com.lockchat.app.service;

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
public final class MeshForegroundService_MembersInjector implements MembersInjector<MeshForegroundService> {
  private final Provider<TransportManager> transportManagerProvider;

  public MeshForegroundService_MembersInjector(
      Provider<TransportManager> transportManagerProvider) {
    this.transportManagerProvider = transportManagerProvider;
  }

  public static MembersInjector<MeshForegroundService> create(
      Provider<TransportManager> transportManagerProvider) {
    return new MeshForegroundService_MembersInjector(transportManagerProvider);
  }

  @Override
  public void injectMembers(MeshForegroundService instance) {
    injectTransportManager(instance, transportManagerProvider.get());
  }

  @InjectedFieldSignature("com.lockchat.app.service.MeshForegroundService.transportManager")
  public static void injectTransportManager(MeshForegroundService instance,
      TransportManager transportManager) {
    instance.transportManager = transportManager;
  }
}
