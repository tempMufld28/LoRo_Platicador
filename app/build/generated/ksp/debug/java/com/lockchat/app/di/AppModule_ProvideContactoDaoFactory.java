package com.lockchat.app.di;

import com.lockchat.app.data.local.AppDatabase;
import com.lockchat.app.data.local.dao.ContactoDao;
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
public final class AppModule_ProvideContactoDaoFactory implements Factory<ContactoDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideContactoDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ContactoDao get() {
    return provideContactoDao(dbProvider.get());
  }

  public static AppModule_ProvideContactoDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideContactoDaoFactory(dbProvider);
  }

  public static ContactoDao provideContactoDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideContactoDao(db));
  }
}
