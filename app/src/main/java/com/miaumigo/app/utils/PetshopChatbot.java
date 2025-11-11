package com.miaumigo.app.utils;

import android.content.Context;

public class PetshopChatbot {
    private final Context context;

    public PetshopChatbot(Context context) {
        this.context = context;
    }

    public String processMessage(String userMessage) {
        String message = userMessage.toLowerCase().trim();

        if (message.matches("[0-9]+")) {
            return handleNumberSelection(message);
        }

        if (message.contains("oi") || message.contains("olá") || message.contains("ola")
                || message.contains("menu") || message.contains("opções") || message.contains("ajuda")) {
            return getMainMenu();
        }

        if (message.contains("produto") || message.contains("ração") || message.contains("brinquedo")
                || message.contains("comprar") || message.contains("loja") || message.contains("categoria")) {
            return getProductsInfo();
        }

        if (message.contains("status") || message.contains("pedido") || message.contains("entrega")
                || message.contains("rastrear") || message.contains("encomenda")) {
            return getOrderStatusInfo();
        }

        if (message.contains("fornecedor") || message.contains("parceria") || message.contains("distribuidor")
                || message.contains("vender") || message.contains("atacado")) {
            return getSupplierInfo();
        }

        if (message.contains("horário") || message.contains("funcionamento") || message.contains("aberto")
                || message.contains("horas") || message.contains("atendimento")) {
            return getBusinessHours();
        }

        if (message.contains("contato") || message.contains("telefone") || message.contains("email")
                || message.contains("falar") || message.contains("suporte") || message.contains("whatsapp")) {
            return getContactInfo();
        }

        if (message.contains("categorias") || message.contains("categoria") || message.contains("tipos")) {
            return getCategories();
        }

        if (message.contains("preço") || message.contains("valor") || message.contains("quanto custa")
                || message.contains("promoção") || message.contains("desconto")) {
            return getPricingInfo();
        }

        if (message.contains("cadastro") || message.contains("cadastrar") || message.contains("registro")) {
            return getSupplierRegistration();
        }

        return "Desculpe, não entendi. Digite [0] para ver o menu principal.";
    }

    private String handleNumberSelection(String number) {
        switch (number) {
            case "1":
                return getProductsInfo();
            case "2":
                return getOrderStatusInfo();
            case "3":
                return getSupplierInfo();
            case "4":
                return getGeneralHelp();
            case "5":
                return getCategories();
            case "6":
                return getContactInfo();
            case "7":
                return getBusinessHours();
            case "8":
                return getPricingInfo();
            case "9":
                return getSupplierRegistration();
            case "0":
                return getMainMenu();
            default:
                return "Opção inválida! Digite [0] para ver o menu principal.";
        }
    }

    public String getMainMenu() {
        return "🐾 **MENU PRINCIPAL - MIAUMIGO SHOP** 🐾\n\n"
                + "Escolha uma opção digitando o número:\n\n"
                + "🛍️  [1] Informações sobre produtos\n"
                + "📦  [2] Status de pedidos\n"
                + "🤝  [3] Área para fornecedores\n"
                + "❓  [4] Dúvidas gerais\n"
                + "📋  [5] Ver categorias\n"
                + "📞  [6] Contato e suporte\n"
                + "⏰  [7] Horário de funcionamento\n"
                + "💰  [8] Informações de preços\n"
                + "📝  [9] Cadastro de fornecedor\n\n"
                + "💡 *Digite 0 para ver este menu novamente*";
    }

    private String getProductsInfo() {
        return "🛍️ **INFORMAÇÕES SOBRE PRODUTOS**\n\n"
                + "No Miaumigo Shop você encontra:\n\n"
                + "• 🐕 Rações premium para cães\n"
                + "• 🐈 Rações premium para gatos\n"
                + "• 🎾 Brinquedos interativos\n"
                + "• 🛏️ Camas e casinhas\n"
                + "• 🧴 Produtos de higiene\n"
                + "• 💊 Medicamentos e vitaminas\n"
                + "• 🧸 Acessórios diversos\n\n"
                + "Digite [0] para voltar ao menu principal.";
    }

    private String getOrderStatusInfo() {
        return "📦 **STATUS DO PEDIDO**\n\n"
                + "Para consultar seu pedido, você precisa do número do pedido.\n\n"
                + "📋 **O que você pode fazer:**\n"
                + "• Acompanhar entrega em tempo real\n"
                + "• Ver histórico de pedidos\n"
                + "• Solicitar segunda via da nota fiscal\n\n"
                + "Digite [0] para voltar ao menu principal.";
    }

    private String getSupplierInfo() {
        return "🤝 **ÁREA PARA FORNECEDORES**\n\n"
                + "Trabalhamos com fornecedores de:\n\n"
                + "• Rações e petiscos\n"
                + "• Medicamentos veterinários\n"
                + "• Acessórios e brinquedos\n"
                + "• Produtos de higiene\n\n"
                + "Digite [0] para voltar ao menu principal.";
    }

    private String getGeneralHelp() {
        return "❓ **DÚVIDAS GERAIS**\n\n"
                + "Aqui estão algumas dúvidas frequentes:\n\n"
                + "📋 **Como comprar?**\n"
                + "• Navegue pelas categorias\n"
                + "• Adicione produtos ao carrinho\n"
                + "• Finalize seu pedido\n\n"
                + "🚚 **Entregas:**\n"
                + "• Entregamos em toda a cidade\n"
                + "• Frete grátis acima de R$ 100,00\n\n"
                + "💳 **Pagamentos:**\n"
                + "• Cartão de crédito/débito\n"
                + "• PIX\n"
                + "• Boleto bancário\n\n"
                + "Digite [0] para voltar ao menu principal.";
    }

    private String getCategories() {
        return "📋 **CATEGORIAS DISPONÍVEIS**\n\n"
                + "🐕 **CÃES**\n"
                + "• Rações\n• Petiscos\n• Brinquedos\n• Coleiras\n\n"
                + "🐈 **GATOS**\n"
                + "• Rações\n• Areias\n• Arranhadores\n• Brinquedos\n\n"
                + "🐦 **PÁSSAROS**\n"
                + "• Alpiste\n• Gaiolas\n• Acessórios\n\n"
                + "🐠 **PEIXES**\n"
                + "• Ração\n• Aquários\n• Filtros\n\n"
                + "🐹 **ROEDORES**\n"
                + "• Rações\n• Gaiolas\n• Acessórios\n\n"
                + "💊 **SAÚDE**\n"
                + "• Medicamentos\n• Vitaminas\n• Antipulgas\n\n"
                + "Digite [0] para voltar ao menu principal.";
    }

    private String getContactInfo() {
        return "📞 **CONTATO E SUPORTE**\n\n"
                + "Estamos aqui para ajudar!\n\n"
                + "📱 **WhatsApp:** (11) 99999-9999\n"
                + "📧 **E-mail:** contato@miaumigoshop.com\n"
                + "📍 **Endereço:** Rua dos Animais, 123 - Centro\n\n"
                + "⏰ **Horário de atendimento:**\n"
                + "Segunda a Sexta: 8h às 18h\n"
                + "Sábados: 8h às 12h\n\n"
                + "Digite [0] para voltar ao menu principal.";
    }

    private String getBusinessHours() {
        return "⏰ **HORÁRIO DE FUNCIONAMENTO**\n\n"
                + "🏪 **Loja Física:**\n"
                + "Segunda a Sexta: 8h às 18h\n"
                + "Sábados: 8h às 12h\n"
                + "Domingos: Fechado\n\n"
                + "🛒 **E-commerce:**\n"
                + "24 horas por dia, 7 dias por semana!\n\n"
                + "📞 **Atendimento:**\n"
                + "Segunda a Sexta: 8h às 18h\n"
                + "Sábados: 8h às 12h\n\n"
                + "Digite [0] para voltar ao menu principal.";
    }

    private String getPricingInfo() {
        return "💰 **INFORMAÇÕES DE PREÇOS**\n\n"
                + "💎 **Nossa política:**\n"
                + "• Preços competitivos\n"
                + "• Qualidade garantida\n"
                + "• Promoções semanais\n\n"
                + "🚚 **Frete grátis:**\n"
                + "Em compras acima de R$ 100,00\n\n"
                + "💳 **Formas de pagamento:**\n"
                + "• Cartão: até 12x sem juros\n"
                + "• PIX: 5% de desconto\n"
                + "• Boleto: à vista\n\n"
                + "Digite [0] para voltar ao menu principal.";
    }

    private String getSupplierRegistration() {
        return "📝 **CADASTRO DE FORNECEDOR**\n\n"
                + "📋 **Como se cadastrar:**\n\n"
                + "1. Acesse 'Área do Fornecedor' no app\n"
                + "2. Preencha o formulário online\n"
                + "3. Envie documentação necessária\n"
                + "4. Nossa equipe entrará em contato\n\n"
                + "📧 **E-mail exclusivo:**\n"
                + "fornecedores@miaumigoshop.com\n\n"
                + "📞 **Telefone comercial:**\n"
                + "(11) 88888-8888\n\n"
                + "Digite [0] para voltar ao menu principal.";
    }
}

