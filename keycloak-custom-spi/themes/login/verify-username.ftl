<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=false displayMessage=false; section>
    <#if section == "title">
        ${msg("loginTitle", (realm.displayName!''))}
    <#elseif section == "form">
        <#if realm.password>
            <div class="container">
                <div id="loginbox" style="margin-top:100px;" class="mainbox col-md-6 col-md-offset-3 col-sm-8 col-sm-offset-2">
                    <div class="panel panel-primary" >
                        <div class="panel-heading">
                            <h3 class="panel-title">${msg("loginTitle", (realm.displayName!''))}</h3>
                        </div>
                        <div class="panel-body" >
                            <#if message?has_content>
                                <div id="login-alert" class="alert alert-danger col-sm-12">
                                    <span class="kc-feedback-text">${kcSanitize(message.summary)?no_esc}</span>
                                </div>
                            </#if>

                            <form id="kc-form-login" class="${properties.kcFormClass!}" onsubmit="login.disabled = true; return true;" action="${url.loginAction?keep_after('^[^#]*?://.*?[^/]*', 'r')}" method="post">
                                <div class="${properties.kcInputWrapperClass!}">
                                    <span class="input-group-addon"><i class="glyphicon glyphicon-user"></i></span>
                                    <input tabindex="1" id="email" class="${properties.kcInputClass!}" name="email" value="" type="email" autofocus autocomplete="off" placeholder="Enter your email" required />
                                </div>

                                <div class="${properties.kcInputWrapperClass!}">
                                    <span class="input-group-addon"><i class="glyphicon glyphicon-lock"></i></span>
                                    <input tabindex="2" id="password" class="${properties.kcInputClass!}" name="password" type="password" placeholder="Enter your password" required />
                                </div>

                                <div id="kc-form-buttons" style="margin-top:10px" class="${properties.kcFormButtonsClass!}">
                                    <div class="${properties.kcFormButtonsWrapperClass!}">
                                        <input tabindex="4" class="${properties.kcButtonClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                                    </div>
                                </div>
                            </form>

                            <div style="margin-top:15px;">
                                <a href="${url.registration}">Create an account</a> |
                                <a href="${url.forgotPassword}">Forgot your password?</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </#if>
    </#if>
</@layout.registrationLayout>