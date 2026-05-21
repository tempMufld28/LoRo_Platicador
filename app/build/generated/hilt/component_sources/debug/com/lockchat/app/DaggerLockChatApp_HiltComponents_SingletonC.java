package com.lockchat.app;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.lockchat.app.data.local.AppDatabase;
import com.lockchat.app.data.local.IdentityDataStore;
import com.lockchat.app.data.local.dao.ContactoDao;
import com.lockchat.app.data.local.dao.MensajeDao;
import com.lockchat.app.data.repository.ContactoRepositoryImpl;
import com.lockchat.app.data.repository.MensajeRepositoryImpl;
import com.lockchat.app.data.transport.TransportManager;
import com.lockchat.app.data.transport.ble.BleTransport;
import com.lockchat.app.data.transport.lora.LoRaUsbTransport;
import com.lockchat.app.di.AppModule_ProvideAppDatabaseFactory;
import com.lockchat.app.di.AppModule_ProvideContactoDaoFactory;
import com.lockchat.app.di.AppModule_ProvideMensajeDaoFactory;
import com.lockchat.app.service.MeshForegroundService;
import com.lockchat.app.service.MeshForegroundService_MembersInjector;
import com.lockchat.app.transport.UsbEventReceiver;
import com.lockchat.app.transport.UsbEventReceiver_MembersInjector;
import com.lockchat.app.ui.screens.addcontact.AddContactViewModel;
import com.lockchat.app.ui.screens.addcontact.AddContactViewModel_HiltModules;
import com.lockchat.app.ui.screens.chatdetail.ChatDetailViewModel;
import com.lockchat.app.ui.screens.chatdetail.ChatDetailViewModel_HiltModules;
import com.lockchat.app.ui.screens.chats.ChatsViewModel;
import com.lockchat.app.ui.screens.chats.ChatsViewModel_HiltModules;
import com.lockchat.app.ui.screens.onboarding.OnboardingViewModel;
import com.lockchat.app.ui.screens.onboarding.OnboardingViewModel_HiltModules;
import com.lockchat.app.ui.screens.ping.PingViewModel;
import com.lockchat.app.ui.screens.ping.PingViewModel_HiltModules;
import com.lockchat.app.ui.screens.profile.ProfileViewModel;
import com.lockchat.app.ui.screens.profile.ProfileViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerLockChatApp_HiltComponents_SingletonC {
  private DaggerLockChatApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public LockChatApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements LockChatApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public LockChatApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements LockChatApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public LockChatApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements LockChatApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public LockChatApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements LockChatApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public LockChatApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements LockChatApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public LockChatApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements LockChatApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public LockChatApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements LockChatApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public LockChatApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends LockChatApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends LockChatApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends LockChatApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends LockChatApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(6).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_addcontact_AddContactViewModel, AddContactViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_chatdetail_ChatDetailViewModel, ChatDetailViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_chats_ChatsViewModel, ChatsViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_onboarding_OnboardingViewModel, OnboardingViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_ping_PingViewModel, PingViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_profile_ProfileViewModel, ProfileViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_lockchat_app_ui_screens_addcontact_AddContactViewModel = "com.lockchat.app.ui.screens.addcontact.AddContactViewModel";

      static String com_lockchat_app_ui_screens_profile_ProfileViewModel = "com.lockchat.app.ui.screens.profile.ProfileViewModel";

      static String com_lockchat_app_ui_screens_onboarding_OnboardingViewModel = "com.lockchat.app.ui.screens.onboarding.OnboardingViewModel";

      static String com_lockchat_app_ui_screens_chatdetail_ChatDetailViewModel = "com.lockchat.app.ui.screens.chatdetail.ChatDetailViewModel";

      static String com_lockchat_app_ui_screens_ping_PingViewModel = "com.lockchat.app.ui.screens.ping.PingViewModel";

      static String com_lockchat_app_ui_screens_chats_ChatsViewModel = "com.lockchat.app.ui.screens.chats.ChatsViewModel";

      @KeepFieldType
      AddContactViewModel com_lockchat_app_ui_screens_addcontact_AddContactViewModel2;

      @KeepFieldType
      ProfileViewModel com_lockchat_app_ui_screens_profile_ProfileViewModel2;

      @KeepFieldType
      OnboardingViewModel com_lockchat_app_ui_screens_onboarding_OnboardingViewModel2;

      @KeepFieldType
      ChatDetailViewModel com_lockchat_app_ui_screens_chatdetail_ChatDetailViewModel2;

      @KeepFieldType
      PingViewModel com_lockchat_app_ui_screens_ping_PingViewModel2;

      @KeepFieldType
      ChatsViewModel com_lockchat_app_ui_screens_chats_ChatsViewModel2;
    }
  }

  private static final class ViewModelCImpl extends LockChatApp_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<AddContactViewModel> addContactViewModelProvider;

    private Provider<ChatDetailViewModel> chatDetailViewModelProvider;

    private Provider<ChatsViewModel> chatsViewModelProvider;

    private Provider<OnboardingViewModel> onboardingViewModelProvider;

    private Provider<PingViewModel> pingViewModelProvider;

    private Provider<ProfileViewModel> profileViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.addContactViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.chatDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.chatsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.onboardingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.pingViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.profileViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(6).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_addcontact_AddContactViewModel, ((Provider) addContactViewModelProvider)).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_chatdetail_ChatDetailViewModel, ((Provider) chatDetailViewModelProvider)).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_chats_ChatsViewModel, ((Provider) chatsViewModelProvider)).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_onboarding_OnboardingViewModel, ((Provider) onboardingViewModelProvider)).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_ping_PingViewModel, ((Provider) pingViewModelProvider)).put(LazyClassKeyProvider.com_lockchat_app_ui_screens_profile_ProfileViewModel, ((Provider) profileViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_lockchat_app_ui_screens_profile_ProfileViewModel = "com.lockchat.app.ui.screens.profile.ProfileViewModel";

      static String com_lockchat_app_ui_screens_chats_ChatsViewModel = "com.lockchat.app.ui.screens.chats.ChatsViewModel";

      static String com_lockchat_app_ui_screens_addcontact_AddContactViewModel = "com.lockchat.app.ui.screens.addcontact.AddContactViewModel";

      static String com_lockchat_app_ui_screens_onboarding_OnboardingViewModel = "com.lockchat.app.ui.screens.onboarding.OnboardingViewModel";

      static String com_lockchat_app_ui_screens_chatdetail_ChatDetailViewModel = "com.lockchat.app.ui.screens.chatdetail.ChatDetailViewModel";

      static String com_lockchat_app_ui_screens_ping_PingViewModel = "com.lockchat.app.ui.screens.ping.PingViewModel";

      @KeepFieldType
      ProfileViewModel com_lockchat_app_ui_screens_profile_ProfileViewModel2;

      @KeepFieldType
      ChatsViewModel com_lockchat_app_ui_screens_chats_ChatsViewModel2;

      @KeepFieldType
      AddContactViewModel com_lockchat_app_ui_screens_addcontact_AddContactViewModel2;

      @KeepFieldType
      OnboardingViewModel com_lockchat_app_ui_screens_onboarding_OnboardingViewModel2;

      @KeepFieldType
      ChatDetailViewModel com_lockchat_app_ui_screens_chatdetail_ChatDetailViewModel2;

      @KeepFieldType
      PingViewModel com_lockchat_app_ui_screens_ping_PingViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.lockchat.app.ui.screens.addcontact.AddContactViewModel 
          return (T) new AddContactViewModel(singletonCImpl.contactoRepositoryImplProvider.get(), singletonCImpl.identityDataStoreProvider.get());

          case 1: // com.lockchat.app.ui.screens.chatdetail.ChatDetailViewModel 
          return (T) new ChatDetailViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.contactoRepositoryImplProvider.get(), singletonCImpl.mensajeRepositoryImplProvider.get(), singletonCImpl.transportManagerProvider.get());

          case 2: // com.lockchat.app.ui.screens.chats.ChatsViewModel 
          return (T) new ChatsViewModel(singletonCImpl.contactoRepositoryImplProvider.get(), singletonCImpl.provideMensajeDaoProvider.get(), singletonCImpl.transportManagerProvider.get());

          case 3: // com.lockchat.app.ui.screens.onboarding.OnboardingViewModel 
          return (T) new OnboardingViewModel(singletonCImpl.identityDataStoreProvider.get());

          case 4: // com.lockchat.app.ui.screens.ping.PingViewModel 
          return (T) new PingViewModel(singletonCImpl.transportManagerProvider.get(), singletonCImpl.contactoRepositoryImplProvider.get());

          case 5: // com.lockchat.app.ui.screens.profile.ProfileViewModel 
          return (T) new ProfileViewModel(singletonCImpl.identityDataStoreProvider.get(), singletonCImpl.contactoRepositoryImplProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends LockChatApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends LockChatApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectMeshForegroundService(MeshForegroundService meshForegroundService) {
      injectMeshForegroundService2(meshForegroundService);
    }

    @CanIgnoreReturnValue
    private MeshForegroundService injectMeshForegroundService2(MeshForegroundService instance) {
      MeshForegroundService_MembersInjector.injectTransportManager(instance, singletonCImpl.transportManagerProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends LockChatApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<BleTransport> bleTransportProvider;

    private Provider<LoRaUsbTransport> loRaUsbTransportProvider;

    private Provider<IdentityDataStore> identityDataStoreProvider;

    private Provider<AppDatabase> provideAppDatabaseProvider;

    private Provider<MensajeDao> provideMensajeDaoProvider;

    private Provider<MensajeRepositoryImpl> mensajeRepositoryImplProvider;

    private Provider<ContactoDao> provideContactoDaoProvider;

    private Provider<ContactoRepositoryImpl> contactoRepositoryImplProvider;

    private Provider<TransportManager> transportManagerProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(Collections.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>emptyMap());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.bleTransportProvider = DoubleCheck.provider(new SwitchingProvider<BleTransport>(singletonCImpl, 1));
      this.loRaUsbTransportProvider = DoubleCheck.provider(new SwitchingProvider<LoRaUsbTransport>(singletonCImpl, 2));
      this.identityDataStoreProvider = DoubleCheck.provider(new SwitchingProvider<IdentityDataStore>(singletonCImpl, 3));
      this.provideAppDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 6));
      this.provideMensajeDaoProvider = DoubleCheck.provider(new SwitchingProvider<MensajeDao>(singletonCImpl, 5));
      this.mensajeRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<MensajeRepositoryImpl>(singletonCImpl, 4));
      this.provideContactoDaoProvider = DoubleCheck.provider(new SwitchingProvider<ContactoDao>(singletonCImpl, 8));
      this.contactoRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<ContactoRepositoryImpl>(singletonCImpl, 7));
      this.transportManagerProvider = DoubleCheck.provider(new SwitchingProvider<TransportManager>(singletonCImpl, 0));
    }

    @Override
    public void injectLockChatApp(LockChatApp lockChatApp) {
      injectLockChatApp2(lockChatApp);
    }

    @Override
    public void injectUsbEventReceiver(UsbEventReceiver usbEventReceiver) {
      injectUsbEventReceiver2(usbEventReceiver);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @CanIgnoreReturnValue
    private LockChatApp injectLockChatApp2(LockChatApp instance) {
      LockChatApp_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      return instance;
    }

    @CanIgnoreReturnValue
    private UsbEventReceiver injectUsbEventReceiver2(UsbEventReceiver instance) {
      UsbEventReceiver_MembersInjector.injectTransportManager(instance, transportManagerProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.lockchat.app.data.transport.TransportManager 
          return (T) new TransportManager(singletonCImpl.bleTransportProvider.get(), singletonCImpl.loRaUsbTransportProvider.get(), singletonCImpl.identityDataStoreProvider.get(), singletonCImpl.mensajeRepositoryImplProvider.get(), singletonCImpl.contactoRepositoryImplProvider.get());

          case 1: // com.lockchat.app.data.transport.ble.BleTransport 
          return (T) new BleTransport(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 2: // com.lockchat.app.data.transport.lora.LoRaUsbTransport 
          return (T) new LoRaUsbTransport(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.lockchat.app.data.local.IdentityDataStore 
          return (T) new IdentityDataStore(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 4: // com.lockchat.app.data.repository.MensajeRepositoryImpl 
          return (T) new MensajeRepositoryImpl(singletonCImpl.provideMensajeDaoProvider.get());

          case 5: // com.lockchat.app.data.local.dao.MensajeDao 
          return (T) AppModule_ProvideMensajeDaoFactory.provideMensajeDao(singletonCImpl.provideAppDatabaseProvider.get());

          case 6: // com.lockchat.app.data.local.AppDatabase 
          return (T) AppModule_ProvideAppDatabaseFactory.provideAppDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 7: // com.lockchat.app.data.repository.ContactoRepositoryImpl 
          return (T) new ContactoRepositoryImpl(singletonCImpl.provideContactoDaoProvider.get());

          case 8: // com.lockchat.app.data.local.dao.ContactoDao 
          return (T) AppModule_ProvideContactoDaoFactory.provideContactoDao(singletonCImpl.provideAppDatabaseProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
