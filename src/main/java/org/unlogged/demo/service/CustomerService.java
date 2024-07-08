package org.unlogged.demo.service;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unlogged.demo.Customer;
import org.unlogged.demo.constants.ScoreConstants;
import org.unlogged.demo.dao.CustomerProfileRepo;
import org.unlogged.demo.models.CustomerProfile;
import org.unlogged.demo.models.CustomerScoreCardMap;
import org.unlogged.demo.repository.CustomerProfileRepository;
import org.unlogged.demo.utils.ScoreUtils;
import org.unlogged.demo.models.CustomerProfileRequest;
import org.unlogged.demo.models.CustomerScoreCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.unlogged.demo.OtelConfig.registerMethod;
import static org.unlogged.demo.utils.ReferralUtils.generateReferralCode;

import static org.unlogged.demo.OtelConfig.makeSpan;


@Service
public class CustomerService {

    private static final Tracer tracer = GlobalOpenTelemetry.getTracer("unlogged-spring-maven-demo");

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private CustomerProfileRepo repo;

    public CustomerProfile fetchCustomerProfile(long id) {
        Span span = tracer.spanBuilder("custom_tracer.19").startSpan();
        makeSpan(span, "input.id", id);

        CustomerProfile profile = customerProfileRepository.fetchCustomerProfile(id);
        makeSpan(span, "mockData.1", profile);

        makeSpan(span, "output", profile);
        span.end();
        return profile;
    }

    public CustomerScoreCard getCustomerScoreCard(long customerId) {
        CustomerScoreCard customerScoreCard = customerProfileRepository.fetchCustomerScoreCard(customerId);
        return customerScoreCard;
    }

    public CustomerProfile saveNewCustomer(CustomerProfileRequest saveRequest) {
        Span span = tracer.spanBuilder("custom_tracer.20").startSpan();
        makeSpan(span, "input.saveRequest", saveRequest);

        List<String> codes = generateReferralCodes();
        makeSpan(span, "mockData.1", codes);
        saveRequest.setCodes(codes);
        CustomerProfile customerProfile = customerProfileRepository.save(saveRequest);
        makeSpan(span, "output", customerProfile);
        span.end();
        return customerProfile;
    }

    public CustomerProfile removeCustomer(long customerID) {
        return customerProfileRepository.removeCustomer(customerID);
    }

    public CustomerProfile generateReferralForCustomer(long customerID) {
        CustomerProfile profile = customerProfileRepository.fetchCustomerProfile(customerID);
        profile.getReferralcodes().add(generateReferralCode());
        customerProfileRepository.save(profile);
        return profile;
    }

    private List<String> generateReferralCodes() {
        Span span = tracer.spanBuilder("custom_tracer.21").startSpan();

        int codeCount = 5;
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < codeCount; i++) {
            codes.add(generateReferralCode());
        }

        makeSpan(span, "output", codes);
        span.end();
        return codes;
    }

    public CustomerScoreCard isCustomerEligibleForPremium(long customerID) {
        CustomerProfile profile = customerProfileRepository.fetchCustomerProfile(customerID);
        int score = 0, bonus = 0;
        boolean isEligible = false;
        if (profile.getAddress().contains("Ohio") ||
                profile.getAddress().contains("Virginia") ||
                profile.getAddress().contains("Texas")) {
            bonus = 2;
        } else {
            bonus = 1;
        }
        score += profile.getReferralcodes().size() * (bonus + ScoreConstants.SCORE_PER_REFERRAL);
        if (score >= ScoreConstants.PREMIUM_CUT_OFF) {
            isEligible = true;
        }
        return new CustomerScoreCard(profile, score, isEligible);
    }

    public List<CustomerProfile> filterEligbleCustomers(List<CustomerProfile> customers, boolean asiaFlow) {
        List<CustomerProfile> eligibleCustomers = new ArrayList<>();
        customers = customerProfileRepository.getAsianCustomers();
        if (asiaFlow) {
            for (CustomerProfile customer : customers) {
                int bonus = 0;
                int score = 0;
                switch (customer.getReferralcodes().size()) {
                    case 0:
                        bonus = 0;
                        break;
                    case 5:
                        if (customer.getAddress().contains("India")) {
                            bonus = 5;
                        }
                        if (customer.getAddress().contains("Nepal")) {
                            bonus = 5;
                        }
                        if (customer.getAddress().contains("Japan")) {
                            bonus = 5;
                        }
                        break;
                    case 6:
                        if (customer.getAddress().contains("Thailand")) {
                            bonus = 5;
                        }
                        break;
                    default:
                        bonus = 2;
                }
                score = ScoreUtils.CalculateScoreForCenosCustomer(customer, bonus);
                if (score >= ScoreConstants.ASIA_CUT_OFF) {
                    eligibleCustomers.add(customer);
                }
            }
        } else {
            for (CustomerProfile customer : customers) {
                int bonus = 1;
                int score = 0;

                if (customer.getReferralcodes().size() == 0) {
                    bonus = 0;
                }
                if (customer.getReferralcodes().size() >= 5) {
                    bonus = 4;
                }
                score = ScoreUtils.CalculateScoreForCenosCustomer(customer, bonus);
                if (score >= ScoreConstants.INTERNATIONAL_CUT_OFF) {
                    eligibleCustomers.add(customer);
                }
            }
        }
        return eligibleCustomers;
    }

    public CustomerProfile getBackProfile(CustomerProfile customerProfile) {
        return customerProfile;
    }

    public List<CustomerScoreCardMap> getDummyScoreMaps() {
        CustomerProfile c = new CustomerProfile();
        CustomerProfile c1 = new CustomerProfile(1, "1", "1", "1", "1", "1", new ArrayList<>());
        CustomerScoreCard customerScoreCard = new CustomerScoreCard(c, 0, false);
        CustomerScoreCard customerScoreCard1 = new CustomerScoreCard(c1, 100, true);

        List<CustomerScoreCard> scoreCards = Arrays.asList(customerScoreCard, customerScoreCard1);
        CustomerScoreCardMap map1 = new CustomerScoreCardMap(scoreCards);

        CustomerProfile c2 = new CustomerProfile();
        CustomerProfile c3 = new CustomerProfile(2, "2", "2", "2", "2", "2", new ArrayList<>());
        CustomerScoreCard customerScoreCard2 = new CustomerScoreCard(c, 44, false);
        CustomerScoreCard customerScoreCard3 = new CustomerScoreCard(c1, 56, true);


        List<CustomerScoreCard> scoreCards2 = Arrays.asList(customerScoreCard2, customerScoreCard3);
        CustomerScoreCardMap map2 = new CustomerScoreCardMap(scoreCards2);

        return Arrays.asList(map1, map2);
    }

    static {
        Span span = tracer.spanBuilder("method_registration").startSpan();
        registerMethod(span, 19, "org.unlogged.demo.service.CustomerService", "fetchCustomerProfile", "J", "org.unlogged.demo.models.CustomerProfile", false, true, true, 1, "(J)Lorg/unlogged/demo/models/CustomerProfile;");
        registerMethod(span, 20, "org.unlogged.demo.service.CustomerService", "saveNewCustomer", "org.unlogged.demo.models.CustomerProfileRequest", "org.unlogged.demo.models.CustomerProfile", false, true, true, 1, "(Lorg/unlogged/demo/models/CustomerProfileRequest;)Lorg/unlogged/demo/models/CustomerProfile;");
        registerMethod(span, 21, "org.unlogged.demo.service.CustomerService", "generateReferralCodes", "", "java.util.List", false, false, true, 2, "()Ljava/util/List;");
        span.end();
    }
}
