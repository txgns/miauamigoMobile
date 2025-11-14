package com.miaumigo.app.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.regex.Pattern;

public class InputMaskHelper {

    /**
     * Aplica máscara de CPF (999.999.999-99)
     */
    public static TextWatcher cpfMask(final EditText editText) {
        return new TextWatcher() {
            boolean isUpdating = false;
            String oldValue = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldValue = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    return;
                }

                String str = s.toString().replaceAll("[^0-9]", "");
                String formatted = "";

                if (str.length() <= 11) {
                    if (str.length() > 3) {
                        formatted = str.substring(0, 3) + ".";
                        if (str.length() > 6) {
                            formatted += str.substring(3, 6) + ".";
                            if (str.length() > 9) {
                                formatted += str.substring(6, 9) + "-" + str.substring(9);
                            } else {
                                formatted += str.substring(6);
                            }
                        } else {
                            formatted += str.substring(3);
                        }
                    } else {
                        formatted = str;
                    }
                } else {
                    formatted = oldValue;
                }

                isUpdating = true;
                editText.setText(formatted);
                editText.setSelection(formatted.length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    /**
     * Aplica máscara de telefone ((99) 99999-9999)
     */
    public static TextWatcher phoneMask(final EditText editText) {
        return new TextWatcher() {
            boolean isUpdating = false;
            String oldValue = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldValue = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    return;
                }

                String str = s.toString().replaceAll("[^0-9]", "");
                String formatted = "";

                if (str.length() <= 11) {
                    if (str.length() > 0) {
                        formatted = "(" + str.substring(0, Math.min(2, str.length()));
                        if (str.length() > 2) {
                            formatted += ") " + str.substring(2, Math.min(7, str.length()));
                            if (str.length() > 7) {
                                formatted += "-" + str.substring(7);
                            }
                        } else {
                            formatted += ") ";
                        }
                    }
                } else {
                    formatted = oldValue;
                }

                isUpdating = true;
                editText.setText(formatted);
                editText.setSelection(formatted.length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    /**
     * Aplica máscara de data (99/99/9999)
     */
    public static TextWatcher dateMask(final EditText editText) {
        return new TextWatcher() {
            boolean isUpdating = false;
            String oldValue = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldValue = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isUpdating) {
                    return;
                }

                String str = s.toString().replaceAll("[^0-9]", "");
                String formatted = "";

                if (str.length() <= 8) {
                    if (str.length() > 0) {
                        formatted = str.substring(0, Math.min(2, str.length()));
                        if (str.length() > 2) {
                            formatted += "/" + str.substring(2, Math.min(4, str.length()));
                            if (str.length() > 4) {
                                formatted += "/" + str.substring(4);
                            }
                        }
                    }
                } else {
                    formatted = oldValue;
                }

                isUpdating = true;
                editText.setText(formatted);
                editText.setSelection(formatted.length());
                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
    }

    /**
     * Remove caracteres especiais de um CPF
     */
    public static String unmaskCpf(String cpf) {
        if (cpf == null) return "";
        return cpf.replaceAll("[^0-9]", "");
    }

    /**
     * Remove caracteres especiais de um telefone
     */
    public static String unmaskPhone(String phone) {
        if (phone == null) return "";
        return phone.replaceAll("[^0-9]", "");
    }

    /**
     * Valida CPF
     */
    public static boolean isValidCpf(String cpf) {
        if (cpf == null) return false;
        
        String cleanCpf = unmaskCpf(cpf);
        
        // Verifica se tem 11 dígitos
        if (cleanCpf.length() != 11) {
            return false;
        }
        
        // Verifica se todos os dígitos são iguais
        if (cleanCpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        // Validação dos dígitos verificadores
        try {
            int[] digits = new int[11];
            for (int i = 0; i < 11; i++) {
                digits[i] = Integer.parseInt(cleanCpf.substring(i, i + 1));
            }
            
            // Calcula o primeiro dígito verificador
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += digits[i] * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit >= 10) firstDigit = 0;
            
            if (firstDigit != digits[9]) {
                return false;
            }
            
            // Calcula o segundo dígito verificador
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += digits[i] * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit >= 10) secondDigit = 0;
            
            return secondDigit == digits[10];
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida telefone (deve ter 10 ou 11 dígitos)
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String cleanPhone = unmaskPhone(phone);
        return cleanPhone.length() == 10 || cleanPhone.length() == 11;
    }
}

