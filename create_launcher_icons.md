# Criar Ícones de Launcher

Como o arquivo `ic_launcher.png` foi deletado, você precisa criar os ícones de launcher. Aqui estão as opções:

## Opção 1: Usar Android Studio (Recomendado)

1. No Android Studio, clique com o botão direito na pasta `app/src/main/res`
2. Selecione `New` → `Image Asset`
3. Configure:
   - **Icon Type**: Launcher Icons (Adaptive and Legacy)
   - **Name**: ic_launcher
   - **Foreground**: Use uma imagem do logo do MiauMigo ou ícone de gato
   - **Background**: Cor sólida (use a cor primária do app: #FF6B35)
4. Clique em `Next` e depois `Finish`

## Opção 2: Usar Gerador Online

1. Acesse: https://romannurik.github.io/AndroidAssetStudio/icons-launcher.html
2. Faça upload de uma imagem do logo MiauMigo
3. Baixe o ZIP gerado
4. Extraia e copie as pastas para `app/src/main/res/`

## Opção 3: Usar Ícones Temporários

Se quiser testar rapidamente, posso criar ícones simples usando drawables XML.

## Estrutura de Pastas Necessária:

```
app/src/main/res/
├── mipmap-mdpi/
│   └── ic_launcher.png
├── mipmap-hdpi/
│   └── ic_launcher.png
├── mipmap-xhdpi/
│   └── ic_launcher.png
├── mipmap-xxhdpi/
│   └── ic_launcher.png
├── mipmap-xxxhdpi/
│   └── ic_launcher.png
└── mipmap-anydpi-v26/
    ├── ic_launcher.xml
    └── ic_launcher_round.xml
```

## Tamanhos dos Ícones:

- **mdpi**: 48x48px
- **hdpi**: 72x72px  
- **xhdpi**: 96x96px
- **xxhdpi**: 144x144px
- **xxxhdpi**: 192x192px

Após criar os ícones, o projeto deve compilar sem erros!
