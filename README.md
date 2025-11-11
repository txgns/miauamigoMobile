# 🐾 MiauMigo Mobile

**E-commerce mobile para produtos pet** - Aplicativo Android nativo desenvolvido em Java com Firebase

[![Android](https://img.shields.io/badge/Android-5.0%2B-green.svg)](https://developer.android.com)
[![Firebase](https://img.shields.io/badge/Firebase-Integrated-orange.svg)](https://firebase.google.com)
[![Java](https://img.shields.io/badge/Java-8-blue.svg)](https://www.java.com)

## 📱 Sobre o Projeto

MiauMigo é um aplicativo mobile de e-commerce especializado em produtos para animais de estimação (cães e gatos). O app oferece uma experiência completa de compra, desde a busca de produtos até a finalização do pedido.

### ✨ Funcionalidades Principais

- 🔐 **Autenticação de Usuários**
  - Login separado para clientes e vendedores
  - Registro com validação de dados
  - Integração com Firebase Authentication

- 🛍️ **Catálogo de Produtos**
  - Listagem de produtos em grade
  - Busca inteligente com sinônimos
  - Detalhes completos do produto
  - Imagens e avaliações

- 🛒 **Carrinho de Compras**
  - Adicionar/remover produtos
  - Ajustar quantidades
  - Cálculo automático de totais
  - Persistência local

- 👤 **Perfil do Usuário**
  - Visualização de dados pessoais
  - Edição de perfil
  - Gerenciamento de endereços
  - Histórico de pedidos

- 🔍 **Busca Avançada**
  - Debounce para otimização
  - Sistema de sinônimos
  - Busca em tempo real
  - Filtros inteligentes

## 🎯 Melhorias Recentes (Outubro 2025)

### ✅ Correções Implementadas

1. **Exibição do Nome do Usuário**
   - ✅ Nome agora aparece corretamente em todas as telas
   - ✅ Integração com Firebase Realtime Database
   - ✅ Fallback para casos de dados não disponíveis

2. **Contraste e Legibilidade**
   - ✅ Todos os textos com cores adequadas (WCAG AA)
   - ✅ Verificação completa de layouts
   - ✅ Consistência visual

3. **Carrinho de Compras**
   - ✅ Corrigido parsing de preços
   - ✅ Formatação brasileira (R$ XX,XX)
   - ✅ Tratamento de erros robusto

4. **Sistema de Busca**
   - ✅ Implementado debounce (300ms)
   - ✅ Sistema de sinônimos
   - ✅ Busca expandida e inteligente

5. **Login de Vendedor**
   - ✅ Sistema de roles implementado
   - ✅ Validação de tipo de conta
   - ✅ Fluxos separados para cliente/vendedor


## 🏗️ Arquitetura

### Tecnologias Utilizadas

- **Linguagem:** Java 8
- **Platform:** Android SDK 21+ (Android 5.0 Lollipop)
- **Build System:** Gradle
- **Backend:** Firebase
  - Authentication
  - Realtime Database
  - Cloud Storage (preparado)
- **Libraries:**
  - Material Components
  - RecyclerView
  - CardView
  - Glide (para imagens)

### Estrutura do Projeto

```
app/src/main/java/com/miaumigo/app/
├── adapters/           # Adapters para RecyclerView
│   ├── CartAdapter.java
│   ├── OrderAdapter.java
│   └── ProductAdapter.java
├── fragments/          # Fragments das telas principais
│   ├── HomeFragment.java
│   ├── ProductsFragment.java
│   ├── CartFragment.java
│   ├── OrdersFragment.java
│   └── ProfileFragment.java
├── models/             # Classes de modelo
│   ├── User.java
│   ├── Product.java
│   ├── CartItem.java
│   ├── Order.java
│   └── Address.java
├── utils/              # Classes utilitárias
│   ├── CartManager.java
│   └── ProductManager.java
├── LoginActivity.java
├── RegisterActivity.java
├── HomeActivity.java
├── ProductDetailActivity.java
└── MainActivity.java
```

## 🚀 Como Executar

### Pré-requisitos

- Android Studio Arctic Fox ou superior
- JDK 8 ou superior
- Conta Firebase (configurada)
- Dispositivo Android ou emulador (API 21+)

### Configuração

1. **Clone o repositório**
```bash
git clone https://github.com/seu-usuario/miauamigoMobile.git
cd miauamigoMobile
```

2. **Configure o Firebase**
   - Acesse o [Firebase Console](https://console.firebase.google.com)
   - Crie um novo projeto ou use existente
   - Adicione um app Android
   - Baixe o arquivo `google-services.json`
   - Coloque em `app/google-services.json`

3. **Abra no Android Studio**
   - File → Open → Selecione a pasta do projeto
   - Aguarde o Gradle sync

4. **Execute o app**
   - Conecte um dispositivo ou inicie um emulador
   - Clique em Run (▶️) ou pressione Shift+F10

## 📦 Build

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

O APK será gerado em:
```
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release.apk
```

## 🔐 Firebase Setup

### Regras do Realtime Database

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    "products": {
      ".read": true,
      ".write": "auth != null && root.child('users').child(auth.uid).child('role').val() === 'vendor'"
    },
    "orders": {
      "$orderId": {
        ".read": "auth.uid === data.child('userId').val()",
        ".write": "auth.uid === newData.child('userId').val()"
      }
    }
  }
}
```

## 👥 Tipos de Usuário

### Cliente (Customer)
- Navegar catálogo de produtos
- Adicionar produtos ao carrinho
- Realizar pedidos
- Visualizar histórico de compras
- Gerenciar perfil e endereços

### Vendedor (Vendor)
- Acesso via login separado
- Base para painel de vendas
- Gestão de produtos (a implementar)
- Visualização de pedidos (a implementar)

## 🎨 Design System

### Cores Principais

| Cor | Hex | Uso |
|-----|-----|-----|
| Primary | `#3498db` | Botões, destaques, cliente |
| Primary Dark | `#2980b9` | Status bar, hover |
| Accent | `#e74c3c` | Vendedor, destaque secundário |
| Success | `#27ae60` | Confirmações, em estoque |
| Error | `#c0392b` | Erros, fora de estoque |
| Text Primary | `#212121` | Textos principais |
| Text Secondary | `#757575` | Textos secundários |
| Background | `#f4f6f9` | Fundo principal |

### Tipografia

- **Títulos:** 24sp, Bold
- **Subtítulos:** 18sp, Bold
- **Corpo:** 16sp, Regular
- **Legendas:** 14sp, Regular
- **Botões:** 16sp, Bold

## 📱 Screenshots

*Em breve...*

## 🧪 Testes

### Fluxos de Teste Principais

1. **Autenticação**
   - Registro de cliente
   - Registro de vendedor
   - Login de cliente
   - Login de vendedor
   - Validação de roles

2. **Produtos**
   - Listar produtos
   - Buscar produtos
   - Ver detalhes
   - Adicionar ao carrinho

3. **Carrinho**
   - Visualizar itens
   - Ajustar quantidades
   - Remover itens
   - Calcular totais

4. **Perfil**
   - Visualizar dados
   - Editar perfil
   - Gerenciar endereços

## 🐛 Problemas Conhecidos

Nenhum problema conhecido no momento. Todas as funcionalidades principais estão operacionais.

## 📈 Roadmap

### Próximas Funcionalidades

- [ ] Painel completo de vendedor
- [ ] Sistema de pagamento
- [ ] Notificações push
- [ ] Chat com vendedor
- [ ] Sistema de avaliações
- [ ] Wishlist
- [ ] Cupons de desconto
- [ ] Programa de fidelidade

## 🤝 Contribuindo

Contribuições são bem-vindas! Sinta-se à vontade para:

1. Fork o projeto
2. Criar uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abrir um Pull Request

## 👨‍💻 Desenvolvedor

Desenvolvido com ❤️ para pets e seus donos

## 📞 Contato

Para dúvidas, sugestões ou problemas:
- Abra uma issue no GitHub
- Entre em contato via e-mail

---

**MiauMigo** - Tudo para o seu pet, na palma da sua mão! 🐕🐈
