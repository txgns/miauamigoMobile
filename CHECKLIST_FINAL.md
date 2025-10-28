# ✅ Checklist de Verificação - MiauMigo Mobile

Use este checklist para verificar se todas as correções estão funcionando corretamente.

---

## 📋 Pré-Requisitos

- [ ] Android Studio instalado e atualizado
- [ ] Firebase configurado (google-services.json no lugar)
- [ ] Dispositivo ou emulador Android (API 21+) disponível
- [ ] Conexão com internet ativa

---

## 🔄 Compilação

### Build do Projeto

- [ ] Abrir projeto no Android Studio
- [ ] Sync do Gradle concluído sem erros
- [ ] Build bem-sucedido (Build > Make Project)
- [ ] Nenhum erro de lint nos arquivos modificados

### Arquivos Verificados

- [ ] `User.java` - Campo `role` adicionado
- [ ] `LoginActivity.java` - Login diferenciado implementado
- [ ] `RegisterActivity.java` - Registro diferenciado implementado
- [ ] `MainActivity.java` - Botões redirecionando corretamente
- [ ] `ProductDetailActivity.java` - Parsing de preço corrigido
- [ ] `ProductsFragment.java` - Busca melhorada implementada
- [ ] `ProfileFragment.java` - Busca de dados do DB implementada
- [ ] `HomeFragment.java` - Busca de dados do DB implementada

---

## 👤 1. Exibição do Nome do Usuário

### Teste de Registro

- [ ] Clicar em "Cliente" na tela inicial
- [ ] Clicar em "Não tem uma conta? Crie uma"
- [ ] Preencher nome: "Seu Nome Completo"
- [ ] Preencher e-mail válido
- [ ] Preencher senha (mínimo 6 caracteres)
- [ ] Confirmar senha
- [ ] Clicar em "Cadastrar"
- [ ] Registro concluído com sucesso

### Verificação de Nome na Home

- [ ] Após login, ver mensagem: "Bem-vindo, [Seu Nome Completo]!"
- [ ] Nome está correto e completo
- [ ] Sem caracteres estranhos ou truncados

### Verificação de Nome no Perfil

- [ ] Navegar para aba "Perfil"
- [ ] Nome aparece no topo da tela
- [ ] E-mail aparece corretamente
- [ ] Telefone aparece (se preenchido) ou "Telefone não informado"

### Teste de Persistência

- [ ] Fazer logout
- [ ] Fazer login novamente com mesma conta
- [ ] Nome ainda aparece corretamente na Home
- [ ] Nome ainda aparece corretamente no Perfil

**✅ Status:** _______________

**📝 Observações:** _______________

---

## 🎨 2. Contraste e Legibilidade

### Tela Home

- [ ] Texto "Bem-vindo" legível
- [ ] Nome do usuário legível
- [ ] Texto "Encontre os melhores produtos" legível
- [ ] Botões com texto legível
- [ ] Card "Ações Rápidas" com texto legível
- [ ] Card "Produtos em Destaque" com texto legível

### Tela de Produtos

- [ ] Placeholder de busca legível
- [ ] Nomes de produtos legíveis
- [ ] Preços legíveis e destacados
- [ ] Avaliações legíveis
- [ ] Status "Em estoque" / "Fora de estoque" legível
- [ ] Mensagem "Nenhum produto encontrado" legível

### Tela de Detalhes do Produto

- [ ] Nome do produto legível
- [ ] Descrição legível
- [ ] Preço legível e destacado
- [ ] Botão "Adicionar ao Carrinho" legível

### Tela do Carrinho

- [ ] Nomes dos produtos legíveis
- [ ] Preços legíveis
- [ ] Quantidades legíveis
- [ ] Total legível e destacado
- [ ] Botão "Finalizar Compra" legível
- [ ] Mensagem "Carrinho vazio" legível (se aplicável)

### Tela de Perfil

- [ ] Nome legível
- [ ] E-mail legível
- [ ] Telefone legível
- [ ] Labels dos ícones legíveis
- [ ] Botões legíveis

**✅ Status:** _______________

**📝 Observações:** _______________

---

## 🛒 3. Carrinho de Compras

### Adicionar Produto

- [ ] Navegar para "Produtos"
- [ ] Clicar em qualquer produto
- [ ] Verificar preço formatado: "R$ XX,XX"
- [ ] Clicar em "Adicionar ao Carrinho"
- [ ] Ver mensagem: "Produto adicionado ao carrinho!"
- [ ] Sem erros ou crashes

### Visualizar Carrinho

- [ ] Navegar para aba "Carrinho"
- [ ] Produto aparece na lista
- [ ] Nome do produto correto
- [ ] Preço formatado: "R$ XX,XX"
- [ ] Quantidade inicial = 1
- [ ] Total do item = preço × quantidade

### Ajustar Quantidade

- [ ] Clicar no botão "+" (aumentar)
- [ ] Quantidade aumenta para 2
- [ ] Total do item atualizado corretamente
- [ ] Clicar no botão "-" (diminuir)
- [ ] Quantidade diminui para 1
- [ ] Total do item atualizado corretamente

### Remover Produto

- [ ] Clicar em "Remover"
- [ ] Produto removido da lista
- [ ] Total geral atualizado
- [ ] Se carrinho vazio, ver mensagem apropriada

### Múltiplos Produtos

- [ ] Adicionar 3 produtos diferentes
- [ ] Todos aparecem no carrinho
- [ ] Cada um com preço correto
- [ ] Total geral = soma de todos os itens
- [ ] Cálculo correto mesmo com quantidades diferentes

### Persistência

- [ ] Adicionar produtos ao carrinho
- [ ] Fechar o app completamente
- [ ] Reabrir o app
- [ ] Carrinho mantém os produtos
- [ ] Quantidades e totais corretos

**✅ Status:** _______________

**📝 Observações:** _______________

---

## 🔍 4. Filtro e Busca de Produtos

### Busca Básica

- [ ] Navegar para "Produtos"
- [ ] Digitar "ração" no campo de busca
- [ ] Aguardar 300ms (debounce)
- [ ] Resultados aparecem filtrados
- [ ] Apenas produtos com "ração" aparecem

### Busca por Sinônimos

**Teste 1: Cachorro**
- [ ] Digitar "dog"
- [ ] Produtos para cachorro aparecem
- [ ] Produtos com "cão", "cachorro", "canino" incluídos

**Teste 2: Gato**
- [ ] Digitar "cat"
- [ ] Produtos para gato aparecem
- [ ] Produtos com "felino", "gato", "gatinho" incluídos

**Teste 3: Comida**
- [ ] Digitar "alimento"
- [ ] Produtos de comida/ração aparecem

**Teste 4: Brinquedo**
- [ ] Digitar "toy"
- [ ] Brinquedos aparecem

### Busca Parcial

- [ ] Digitar "cam" (parcial)
- [ ] Produtos com "caminha", "cama", "camisa" aparecem
- [ ] Busca não exige palavra completa

### Debounce (Delay)

- [ ] Começar a digitar rapidamente
- [ ] Busca não executa a cada letra
- [ ] Aguarda pausa de 300ms
- [ ] Apenas última busca é executada
- [ ] Melhor performance (menos chamadas)

### Sem Resultados

- [ ] Digitar "xpto123abc"
- [ ] Ver mensagem: "Nenhum produto encontrado"
- [ ] Nenhum erro ou crash
- [ ] Possível limpar busca e tentar novamente

### Limpeza de Busca

- [ ] Fazer uma busca qualquer
- [ ] Limpar campo de busca
- [ ] Todos os produtos voltam a aparecer
- [ ] Lista completa restaurada

**✅ Status:** _______________

**📝 Observações:** _______________

---

## 🧾 5. Login de Vendedor

### Registro como Vendedor

- [ ] Na tela inicial, clicar em "Vendedor"
- [ ] Ver subtítulo: "Como Vendedor" (cor vermelha)
- [ ] Clicar em "Não tem uma conta? Crie uma"
- [ ] Preencher dados do vendedor
- [ ] Clicar em "Cadastrar"
- [ ] Ver mensagem: "Cadastro de vendedor realizado com sucesso!"
- [ ] Redirecionado para Home

### Login como Vendedor

- [ ] Fazer logout
- [ ] Na tela inicial, clicar em "Vendedor"
- [ ] Ver subtítulo: "Como Vendedor"
- [ ] Preencher e-mail do vendedor
- [ ] Preencher senha
- [ ] Clicar em "Entrar"
- [ ] Login bem-sucedido
- [ ] Redirecionado para Home

### Validação: Vendedor tentando login de Cliente

- [ ] Estar logado como vendedor (ou ter conta vendedor)
- [ ] Fazer logout
- [ ] Na tela inicial, clicar em "Cliente"
- [ ] Tentar login com conta de vendedor
- [ ] Ver mensagem de erro: "Esta conta é de vendedor. Use o login de vendedor."
- [ ] Login não permitido
- [ ] Usuário deslogado

### Validação: Cliente tentando login de Vendedor

- [ ] Ter uma conta de cliente
- [ ] Na tela inicial, clicar em "Vendedor"
- [ ] Tentar login com conta de cliente
- [ ] Ver mensagem de erro: "Esta conta não é de vendedor. Use o login de cliente."
- [ ] Login não permitido
- [ ] Usuário deslogado

### Diferenciação Visual

- [ ] Login de Cliente: subtítulo "Como Cliente" (azul)
- [ ] Login de Vendedor: subtítulo "Como Vendedor" (vermelho)
- [ ] Cores diferentes nos botões principais
- [ ] Interface clara sobre qual tipo de login

**✅ Status:** _______________

**📝 Observações:** _______________

---

## 🔄 Testes de Integração

### Fluxo Completo: Cliente

1. **Registro**
   - [ ] Registrar como cliente
   - [ ] Nome preenchido corretamente

2. **Home**
   - [ ] Ver nome na mensagem de boas-vindas
   - [ ] Interface legível

3. **Produtos**
   - [ ] Buscar "ração"
   - [ ] Ver resultados filtrados
   - [ ] Textos legíveis

4. **Detalhes**
   - [ ] Clicar em produto
   - [ ] Ver preço formatado
   - [ ] Adicionar ao carrinho

5. **Carrinho**
   - [ ] Ver produto no carrinho
   - [ ] Ajustar quantidade
   - [ ] Ver total correto

6. **Perfil**
   - [ ] Ver dados pessoais
   - [ ] Nome exibido corretamente

### Fluxo Completo: Vendedor

1. **Registro**
   - [ ] Registrar como vendedor
   - [ ] Login correto

2. **Validação**
   - [ ] Logout
   - [ ] Tentar login de cliente
   - [ ] Ver erro apropriado
   - [ ] Login com vendedor funciona

**✅ Status:** _______________

**📝 Observações:** _______________

---

## 🐛 Testes de Edge Cases

### Dados Vazios/Nulos

- [ ] Usuário sem nome no banco → mostra "Usuário"
- [ ] Produto sem imagem → mostra placeholder
- [ ] Carrinho vazio → mostra mensagem apropriada
- [ ] Busca vazia → mostra todos os produtos

### Formatação de Preços

- [ ] Preço R$ 10,00 → exibido corretamente
- [ ] Preço R$ 1.234,56 → exibido corretamente
- [ ] Preço R$ 0,99 → exibido corretamente

### Conexão

- [ ] Sem internet no registro → erro amigável
- [ ] Sem internet no login → erro amigável
- [ ] Sem internet ao carregar produtos → erro amigável

### Erros de Autenticação

- [ ] Senha incorreta → mensagem de erro
- [ ] E-mail não cadastrado → mensagem de erro
- [ ] E-mail inválido → mensagem de erro
- [ ] Senha muito curta → mensagem de erro
- [ ] Senhas não coincidem → mensagem de erro

**✅ Status:** _______________

**📝 Observações:** _______________

---

## 📊 Resumo Final

### Estatísticas

- **Total de testes:** _____
- **Testes passados:** _____
- **Testes falhados:** _____
- **Taxa de sucesso:** _____%

### Problemas Encontrados

1. _______________________________________________
2. _______________________________________________
3. _______________________________________________

### Observações Gerais

__________________________________________________
__________________________________________________
__________________________________________________
__________________________________________________

---

## ✅ Aprovação Final

- [ ] Todas as funcionalidades principais funcionam
- [ ] Interface legível e com bom contraste
- [ ] Sem crashes ou erros críticos
- [ ] Pronto para uso/deploy

**Aprovado por:** _______________  
**Data:** _____/_____/_____  
**Assinatura:** _______________

---

## 📞 Próximos Passos

Após completar este checklist:

1. [ ] Corrigir problemas encontrados (se houver)
2. [ ] Fazer novo ciclo de testes
3. [ ] Gerar APK de release
4. [ ] Testar em dispositivo real
5. [ ] Coletar feedback de usuários beta
6. [ ] Preparar para deploy na Play Store

---

**Checklist criado em:** Outubro 2025  
**Versão do app:** 1.0.0  
**Build:** Debug

🎉 **Boa sorte com os testes!**

