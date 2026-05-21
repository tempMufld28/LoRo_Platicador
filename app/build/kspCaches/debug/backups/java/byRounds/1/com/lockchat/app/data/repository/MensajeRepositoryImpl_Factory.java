package com.lockchat.app.data.repository;

import com.lockchat.app.data.local.dao.MensajeDao;
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
public final class MensajeRepositoryImpl_Factory implements Factory<MensajeRepositoryImpl> {
  private final Provider<MensajeDao> daoProvider;

  public MensajeRepositoryImpl_Factory(Provider<MensajeDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public MensajeRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static MensajeRepositoryImpl_Factory create(Provider<MensajeDao> daoProvider) {
    return new MensajeRepositoryImpl_Factory(daoProvider);
  }

  public static MensajeRepositoryImpl newInstance(MensajeDao dao) {
    return new MensajeRepositoryImpl(dao);
  }
}
