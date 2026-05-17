package com.github.hamzaelalaouiismaili.chari.client.api;

import com.github.hamzaelalaouiismaili.chari.client.core.ChariHttpClient;
import com.github.hamzaelalaouiismaili.chari.domain.enums.ChariClosureReason;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCreatePinPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCustomerConfirmPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariLoginWithPinPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariRegisterCustomerPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariUnregisterCustomerPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariUpdatePinPayload;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBalanceResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariBooleanResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCustomerInfoResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariCustomerStatusResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariDefaultWalletResponse;
import com.github.hamzaelalaouiismaili.chari.model.response.ChariLoginWithPinResponse;
import com.github.hamzaelalaouiismaili.chari.util.PhoneNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Customer registration, status, OTP, PIN, and profile operations.
 */
@Slf4j
@RequiredArgsConstructor
public class ChariCustomerRegistrationClient {

        private final ChariHttpClient httpClient;

        public ChariCustomerStatusResponse getCustomerStatus(String phoneNumber) {
                String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
                log.debug("Getting customer status for phone: {}", PhoneNumberUtil.mask(normalizedPhone));

                String url = UriComponentsBuilder.fromPath("/api/customers/status")
                                .queryParam("phoneNumber", normalizedPhone)
                                .toUriString();
                return httpClient.get(url, ChariCustomerStatusResponse.class, "GET_CUSTOMER_STATUS");
        }

        public ChariDefaultWalletResponse checkDefaultWallet(String phoneNumber) {
                String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
                log.debug("Checking default wallet for phone: {}", PhoneNumberUtil.mask(normalizedPhone));

                String url = UriComponentsBuilder.fromPath("/api/customers/default")
                                .queryParam("phoneNumber", normalizedPhone)
                                .toUriString();
                return httpClient.get(url, ChariDefaultWalletResponse.class, "CHECK_DEFAULT_WALLET");
        }

        public boolean isDefaultWallet(String phoneNumber) {
                ChariDefaultWalletResponse response = checkDefaultWallet(phoneNumber);
                return response != null
                                && response.getData() != null
                                && Boolean.TRUE.equals(response.getData().getIsDefaultWallet());
        }

        public ChariCustomerInfoResponse getCustomerInfo(String phoneNumber) {
                String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
                log.debug("Getting customer info for phone: {}", PhoneNumberUtil.mask(normalizedPhone));

                String url = UriComponentsBuilder.fromPath("/api/customers/info")
                                .queryParam("phoneNumber", normalizedPhone)
                                .toUriString();
                return httpClient.get(url, ChariCustomerInfoResponse.class, "GET_CUSTOMER_INFO");
        }

        public ChariBalanceResponse getCustomerBalance(String phoneNumber) {
                String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
                log.debug("Getting customer balance for phone: {}", PhoneNumberUtil.mask(normalizedPhone));

                String url = UriComponentsBuilder.fromPath("/api/customers/balance")
                                .queryParam("phoneNumber", normalizedPhone)
                                .toUriString();
                return httpClient.get(url, ChariBalanceResponse.class, "GET_CUSTOMER_BALANCE");
        }

        public ChariBooleanResponse unregisterCustomer(String phoneNumber, ChariClosureReason reason) {
                return unregisterCustomer(ChariUnregisterCustomerPayload.builder()
                                .phoneNumber(phoneNumber)
                                .reason(reason)
                                .build());
        }

        public ChariBooleanResponse unregisterCustomer(ChariUnregisterCustomerPayload payload) {
                String normalizedPhone = PhoneNumberUtil.normalize(payload.getPhoneNumber());
                log.debug("Unregistering customer with phone: {}", PhoneNumberUtil.mask(normalizedPhone));

                ChariUnregisterCustomerPayload normalizedPayload = ChariUnregisterCustomerPayload.builder()
                                .phoneNumber(normalizedPhone)
                                .reason(payload.getReason())
                                .build();

                return httpClient.put("/api/customers/unregister", normalizedPayload, ChariBooleanResponse.class,
                                "UNREGISTER_CUSTOMER");
        }

        public ChariBooleanResponse registerCustomer(ChariRegisterCustomerPayload payload) {
                log.debug("Registering customer with phone: {}", PhoneNumberUtil.mask(payload.getPhoneNumber()));

                ChariRegisterCustomerPayload normalizedPayload = ChariRegisterCustomerPayload.builder()
                                .phoneNumber(PhoneNumberUtil.normalize(payload.getPhoneNumber()))
                                .email(payload.getEmail())
                                .firstName(payload.getFirstName())
                                .lastName(payload.getLastName())
                                .cin(payload.getCin())
                                .walletType(payload.getWalletType())
                                .closeLoopOnly(payload.getCloseLoopOnly())
                                .build();

                return httpClient.post("/api/customers/register", normalizedPayload, ChariBooleanResponse.class,
                                "REGISTER_CUSTOMER");
        }

        public ChariBooleanResponse confirmCustomer(ChariCustomerConfirmPayload payload) {
                log.debug("Confirming customer with phone: {}", PhoneNumberUtil.mask(payload.getPhoneNumber()));

                ChariCustomerConfirmPayload normalizedPayload = ChariCustomerConfirmPayload.builder()
                                .phoneNumber(PhoneNumberUtil.normalize(payload.getPhoneNumber()))
                                .code(payload.getCode())
                                .walletType(payload.getWalletType())
                                .autoActivate(payload.getAutoActivate())
                                .build();

                return httpClient.post("/api/customers/confirm", normalizedPayload, ChariBooleanResponse.class,
                                "CONFIRM_CUSTOMER");
        }

        public ChariBooleanResponse resendCustomerOtp(String phoneNumber) {
                String normalizedPhone = PhoneNumberUtil.normalize(phoneNumber);
                log.debug("Resending OTP for phone: {}", PhoneNumberUtil.mask(normalizedPhone));

                String url = UriComponentsBuilder.fromPath("/api/customers/confirm/resend-otp")
                                .queryParam("phoneNumber", normalizedPhone)
                                .toUriString();
                return httpClient.post(url, null, ChariBooleanResponse.class, "RESEND_OTP");
        }

        public ChariLoginWithPinResponse loginWithPin(ChariLoginWithPinPayload payload) {
                log.debug("Logging in customer with phone: {}", PhoneNumberUtil.mask(payload.getPhoneNumber()));

                ChariLoginWithPinPayload normalizedPayload = ChariLoginWithPinPayload.builder()
                                .phoneNumber(PhoneNumberUtil.normalize(payload.getPhoneNumber()))
                                .pin(payload.getPin())
                                .build();

                return httpClient.post("/api/customers/login", normalizedPayload, ChariLoginWithPinResponse.class,
                                "LOGIN_WITH_PIN");
        }

        public ChariLoginWithPinResponse loginWithPin(String phoneNumber, String pin) {
                return loginWithPin(ChariLoginWithPinPayload.builder()
                                .phoneNumber(phoneNumber)
                                .pin(pin)
                                .build());
        }

        public ChariBooleanResponse createPin(String phoneNumber, String pin) {
                return createPin(ChariCreatePinPayload.builder()
                                .phoneNumber(phoneNumber)
                                .pin(pin)
                                .build());
        }

        public ChariBooleanResponse createPin(ChariCreatePinPayload payload) {
                String normalizedPhone = PhoneNumberUtil.normalize(payload.getPhoneNumber());
                log.debug("Creating PIN for phone: {}", PhoneNumberUtil.mask(normalizedPhone));

                ChariCreatePinPayload normalizedPayload = ChariCreatePinPayload.builder()
                                .phoneNumber(normalizedPhone)
                                .pin(payload.getPin())
                                .build();

                return httpClient.post("/api/customers/pin", normalizedPayload, ChariBooleanResponse.class,
                                "CREATE_PIN");
        }

        public ChariBooleanResponse updatePin(String phoneNumber, String oldPin, String newPin) {
                return updatePin(ChariUpdatePinPayload.builder()
                                .phoneNumber(phoneNumber)
                                .oldPin(oldPin)
                                .newPin(newPin)
                                .build());
        }

        public ChariBooleanResponse updatePin(ChariUpdatePinPayload payload) {
                String normalizedPhone = PhoneNumberUtil.normalize(payload.getPhoneNumber());
                log.debug("Updating PIN for phone: {}", PhoneNumberUtil.mask(normalizedPhone));

                ChariUpdatePinPayload normalizedPayload = ChariUpdatePinPayload.builder()
                                .phoneNumber(normalizedPhone)
                                .oldPin(payload.getOldPin())
                                .newPin(payload.getNewPin())
                                .build();

                return httpClient.patch("/api/customers/pin/change", normalizedPayload, ChariBooleanResponse.class,
                                "UPDATE_PIN");
        }
}
