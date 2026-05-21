package com.lockchat.app.data.local;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class IdentityDataStore_Factory implements Factory<IdentityDataStore> {
  private final Provider<Context> contextProvider;

  public IdentityDataStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public IdentityDataStore get() {
    return newInstance(contextProvider.get());
  }

  public static IdentityDataStore_Factory create(Provider<Context> contextProvider) {
    return new IdentityDataStore_Factory(contextProvider);
  }

  public static IdentityDataStore newInstance(Context context) {
    return new IdentityDataStore(context);
  }
}
