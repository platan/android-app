package com.hana053.micropost.pages.login

import com.hana053.micropost.service.LoginService
import com.hana053.micropost.service.Navigator
import com.hana053.micropost.withProgressDialog
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import rx.Observable

class LoginPresenter(
    private val loginService: LoginService,
    private val navigator: Navigator
) {

    fun bind(view: LoginView) {
        val emailChanges = view.emailChanges.share()
        val passwordChanges = view.passwordChanges.share()

        val credentials = Observable.combineLatest(emailChanges, passwordChanges, { email, password ->
            Triple(email, password, email.isNotBlank() && password.isNotBlank())
        })

        credentials
            .bindToLifecycle(view.content)
            .map { it.third }
            .subscribe { view.loginEnabled.call(it) }

        view.loginClicks
            .bindToLifecycle(view.content)
            .withLatestFrom(credentials, { click, credentials ->
                credentials.first to credentials.second
            })
            .flatMap {
                loginService.login(it.first, it.second)
                    .withProgressDialog(view.content)
            }
            .subscribe { navigator.navigateToMain() }
    }

}
