package com.lockchat.app.data.repository;

import com.lockchat.app.data.local.dao.ContactoDao;
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
public final class ContactoRepositoryImpl_Factory implements Factory<ContactoRepositoryImpl> {
  private final Provider<ContactoDao> daoProvider;

  public ContactoRepositoryImpl_Factory(Provider<ContactoDao> daoProvider) {
    this.daoProvider = daoProvider;
  }

  @Override
  public ContactoRepositoryImpl get() {
    return newInstance(daoProvider.get());
  }

  public static ContactoRepositoryImpl_Factory create(Provider<ContactoDao> daoProvider) {
    return new ContactoRepositoryImpl_Factory(daoProvider);
  }

  public static ContactoRepositoryImpl newInstance(ContactoDao dao) {
    return new ContactoRepositoryImpl(dao);
  }
}
