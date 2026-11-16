# MR: Meu Ritmo App

Aplicativo Android do [Meu Ritmo](https://meuritmo.net/app), distribuído fora da Google Play Store.

A tela inicial do APK abre o serviço online em `https://meuritmo.net/app`. O login, as rotinas, as finanças, a comunidade e as assinaturas utilizam os mesmos dados do site, conforme as permissões da conta. Mudanças no site ficam disponíveis nesta tela sem necessidade de recompilar o APK.

A opção **Alarmes do aparelho** abre um módulo local, acessível pelo menu do aplicativo Android. Alarmes, notas e demais dados criados nesse módulo offline ficam no aparelho e **não são sincronizados automaticamente** com a conta online. Faça backup dos dados locais antes de trocar de aparelho ou limpar o armazenamento.

## Android e iPhone

- [Baixar APK oficial 1.0.1](https://github.com/JustJao1/meu-ritmo-app/releases/tag/v1.0.1) para Android 8 ou superior.
- No iPhone, abra o [Meu Ritmo](https://meuritmo.net/app) pelo Safari e use **Adicionar à Tela de Início**. Não há um IPA distribuído.

O diretório `dist` contém a página de instalação. `app-web` mantém o módulo local e `android` reúne o contêiner online, os alarmes nativos e a política de atualização.

## Nova versão nativa

Execute os testes antes de criar outra versão. Para atualizar um APK já instalado, é necessário recompilar e assinar com **a mesma chave privada** usada na versão anterior. O fluxo de preparação está em `.github/workflows/android-release.yml` e o script local em `scripts/release-android.ps1`. Não publique APK não assinado, com outra chave ou com a versão anterior.

Os alarmes dependem das permissões do Android e podem sofrer restrições de bateria, volume e do fabricante; não funcionam com o aparelho desligado.
