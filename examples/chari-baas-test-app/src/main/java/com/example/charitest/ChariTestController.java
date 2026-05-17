package com.example.charitest;

import com.github.hamzaelalaouiismaili.chari.client.ChariBaasClient;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCardCashinPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariCustomerConfirmPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariRegisterCustomerPayload;
import com.github.hamzaelalaouiismaili.chari.model.payload.ChariTransferPayload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/test/chari")
public class ChariTestController {

    private final ChariBaasClient chari;

    public ChariTestController(ChariBaasClient chari) {
        this.chari = chari;
    }

    @GetMapping("/status")
    public Object getStatus(@RequestParam String phoneNumber) {
        return chari.getCustomerStatus(phoneNumber);
    }

    @GetMapping("/default-wallet")
    public Object checkDefaultWallet(@RequestParam String phoneNumber) {
        return chari.checkDefaultWallet(phoneNumber);
    }

    @GetMapping("/balance")
    public Object getBalance(@RequestParam String phoneNumber) {
        return chari.getCustomerBalance(phoneNumber);
    }

    @GetMapping("/agent-wallet")
    public Object getPrincipalAgentWallet() {
        return chari.getPrincipalAgentInfo(null);
    }

    @GetMapping("/info")
    public Object getInfo(@RequestParam String phoneNumber) {
        return chari.getCustomerInfo(phoneNumber);
    }

    @PostMapping("/customers/register")
    public Object register(@RequestBody ChariRegisterCustomerPayload payload) {
        return chari.registerCustomer(payload);
    }

    @PostMapping("/customers/confirm")
    public Object confirm(@RequestBody ChariCustomerConfirmPayload payload) {
        return chari.confirmCustomer(payload);
    }

    @PostMapping("/customers/resend-otp")
    public Object resendOtp(@RequestParam String phoneNumber) {
        return chari.resendCustomerOtp(phoneNumber);
    }

    @PostMapping("/customers/login")
    public Object login(@RequestParam String phoneNumber, @RequestParam String pin) {
        return chari.loginWithPin(phoneNumber, pin);
    }

    @PostMapping("/customers/pin")
    public Object createPin(@RequestParam String phoneNumber, @RequestParam String pin) {
        return chari.createPin(phoneNumber, pin);
    }

    @PatchMapping("/customers/pin")
    public Object updatePin(
            @RequestParam String phoneNumber,
            @RequestParam String oldPin,
            @RequestParam String newPin) {
        return chari.updatePin(phoneNumber, oldPin, newPin);
    }

    @PostMapping("/transfer/preview")
    public Object previewTransfer(@RequestBody ChariTransferPayload payload) {
        return chari.previewTransfer(payload);
    }

    @PostMapping("/transfer/execute")
    public Object executeTransfer(@RequestBody ChariTransferPayload payload) {
        return chari.executeTransfer(payload);
    }

    @PostMapping("/card/cashin/preview")
    public Object previewCardCashin(@RequestParam String phoneNumber, @RequestBody AmountRequest request) {
        return chari.previewCardFunding(phoneNumber, request.amount());
    }

    @PostMapping("/card/cashin/execute")
    public Object executeCardCashin(@RequestParam String phoneNumber, @RequestBody ChariCardCashinPayload payload) {
        return chari.executeCardFunding(phoneNumber, payload);
    }

    @GetMapping("/card/accept")
    public Map<String, String> cardAccept(@RequestParam Map<String, String> params) {
        return params;
    }

    @GetMapping("/card/decline")
    public Map<String, String> cardDecline(@RequestParam Map<String, String> params) {
        return params;
    }

    public record AmountRequest(BigDecimal amount) {
    }
}
