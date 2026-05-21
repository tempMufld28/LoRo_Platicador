package com.lockchat.app.di;

import com.lockchat.app.data.local.AppDatabase;
import com.lockchat.app.data.local.dao.MensajeDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideMensajeDaoFactory implements Factory<MensajeDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideMensajeDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MensajeDao get() {
    return provideMensajeDao(dbProvider.get());
  }

  public static AppModule_ProvideMensajeDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideMensajeDaoFactory(dbProvider);
  }

  public static MensajeDao provideMensajeDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMensajeDao(db));
  }
}
