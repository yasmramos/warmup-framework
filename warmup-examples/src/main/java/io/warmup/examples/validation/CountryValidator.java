package io.warmup.examples.validation;

import io.warmup.framework.annotation.validation.CustomConstraint;
import io.warmup.framework.annotation.validation.CustomConstraintValidator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Custom validator for country codes.
 * Validates that country codes are valid ISO 3166-1 alpha-2 codes.
 * 
 * <p>Parameters:
 * <ul>
 * <li>Optional list of allowed country codes (defaults to common ones if not specified)</li>
 * </ul>
 * 
 * @author MiniMax Agent
 * @since 1.0
 */
public class CountryValidator implements CustomConstraintValidator {
    
    // Set of valid ISO 3166-1 alpha-2 country codes
    private static final Set<String> VALID_COUNTRY_CODES = new HashSet<>(Arrays.asList(
        // Common countries
        "US", "CA", "GB", "UK", "DE", "FR", "IT", "ES", "PT", "NL", "BE", "CH", "AT", "SE", 
        "NO", "DK", "FI", "IE", "LU", "MT", "CY", "EE", "LV", "LT", "PL", "CZ", "SK", "HU", 
        "SI", "HR", "BG", "RO", "GR", "JP", "CN", "IN", "KR", "AU", "NZ", "BR", "MX", "AR", 
        "CL", "CO", "PE", "VE", "RU", "TR", "IL", "ZA", "EG", "MA", "NG", "KE", "GH", "TN",
        // Add more as needed
        "AD", "AE", "AF", "AG", "AI", "AL", "AM", "AO", "AQ", "AS", "AW", "AX", "AZ", "BA",
        "BB", "BD", "BF", "BG", "BH", "BI", "BJ", "BL", "BM", "BN", "BO", "BQ", "BS", "BT",
        "BV", "BW", "BY", "BZ", "CC", "CD", "CF", "CG", "CH", "CI", "CK", "CL", "CM", "CN",
        "CO", "CR", "CU", "CV", "CW", "CX", "CY", "CZ", "DE", "DJ", "DK", "DM", "DO", "DZ",
        "EC", "EE", "EG", "EH", "ER", "ES", "ET", "FI", "FJ", "FK", "FM", "FO", "FR", "GA",
        "GB", "GD", "GE", "GF", "GG", "GH", "GI", "GL", "GM", "GN", "GP", "GQ", "GR", "GS",
        "GT", "GU", "GW", "GY", "HK", "HM", "HN", "HR", "HT", "HU", "ID", "IE", "IL", "IM",
        "IN", "IO", "IQ", "IR", "IS", "IT", "JE", "JM", "JO", "JP", "KE", "KG", "KH", "KI",
        "KM", "KN", "KP", "KR", "KW", "KY", "KZ", "LA", "LB", "LC", "LI", "LK", "LR", "LS",
        "LT", "LU", "LV", "LY", "MA", "MC", "MD", "ME", "MF", "MG", "MH", "MK", "ML", "MM",
        "MN", "MO", "MP", "MQ", "MR", "MS", "MT", "MU", "MV", "MW", "MX", "MY", "MZ", "NA",
        "NC", "NE", "NF", "NG", "NI", "NL", "NO", "NP", "NR", "NU", "NZ", "OM", "PA", "PE",
        "PF", "PG", "PH", "PK", "PL", "PM", "PN", "PR", "PS", "PT", "PW", "PY", "QA", "RE",
        "RO", "RS", "RU", "RW", "SA", "SB", "SC", "SD", "SE", "SG", "SH", "SI", "SJ", "SK",
        "SL", "SM", "SN", "SO", "SR", "SS", "ST", "SV", "SX", "SY", "SZ", "TC", "TD", "TF",
        "TG", "TH", "TJ", "TK", "TL", "TM", "TN", "TO", "TR", "TT", "TV", "TW", "TZ", "UA",
        "UG", "UM", "US", "UY", "UZ", "VA", "VC", "VE", "VG", "VI", "VN", "VU", "WF", "WS",
        "YE", "YT", "ZA", "ZM", "ZW"
    ));
    
    private Set<String> allowedCountries = new HashSet<>();
    
    @Override
    public void initialize(Object... parameters) {
        allowedCountries.clear();
        
        if (parameters != null && parameters.length > 0) {
            // Use provided country codes as allowed list
            for (Object param : parameters) {
                if (param != null) {
                    String country = param.toString().toUpperCase().trim();
                    if (isValidCountryCode(country)) {
                        allowedCountries.add(country);
                    }
                }
            }
        }
        
        // If no specific countries provided, use default set of common countries
        if (allowedCountries.isEmpty()) {
            allowedCountries.addAll(Arrays.asList("US", "CA", "GB", "UK", "DE", "FR", "IT", "ES", "JP", "AU"));
        }
    }
    
    @Override
    public boolean isValid(Object value, Object... parameters) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }
        
        String countryCode = value.toString().toUpperCase().trim();
        if (countryCode.isEmpty()) {
            return false; // Empty country codes are not allowed
        }
        
        // Check if it's a valid ISO country code
        if (!isValidCountryCode(countryCode)) {
            return false;
        }
        
        // Check if it's in the allowed list (if specified)
        if (!allowedCountries.isEmpty() && !allowedCountries.contains(countryCode)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public String getMessage(Object invalidValue, String fieldName, Object... parameters) {
        String countryCode = invalidValue != null ? invalidValue.toString() : "null";
        
        if (countryCode == null || countryCode.trim().isEmpty()) {
            return String.format("Country code cannot be empty for field '%s'", fieldName);
        }
        
        String normalizedCode = countryCode.toUpperCase().trim();
        
        if (!isValidCountryCode(normalizedCode)) {
            return String.format("Invalid country code '%s' for field '%s'. Must be a valid ISO 3166-1 alpha-2 code", 
                               countryCode, fieldName);
        }
        
        if (!allowedCountries.isEmpty() && !allowedCountries.contains(normalizedCode)) {
            String allowedList = String.join(", ", allowedCountries);
            return String.format("Country code '%s' is not allowed for field '%s'. Allowed countries: %s", 
                               countryCode, fieldName, allowedList);
        }
        
        return String.format("Invalid country code for field '%s': '%s'", fieldName, countryCode);
    }
    
    /**
     * Check if a country code is valid according to ISO 3166-1 alpha-2 standard.
     */
    private boolean isValidCountryCode(String code) {
        return code != null && 
               code.length() == 2 && 
               code.matches("^[A-Z]{2}$") && 
               VALID_COUNTRY_CODES.contains(code);
    }
}