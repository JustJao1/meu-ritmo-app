# Auditoria de licenças e atribuições — 26/11/2026

Projeto: meu-ritmo-app. Responsável declarado: João Victor da Silva (JustJao1).

Revisão documental dos arquivos acessíveis no GitHub. Preserva os avisos existentes e os direitos de terceiros; não concede nova licença ao código próprio nem certifica conformidade integral. Nenhuma alteração em código executável ou contrato de serviço.

- package.json não declara dependências npm. O build Android utiliza Android Gradle Plugin 8.13.0 e SDK 36; não há bloco dependencies no android/app/build.gradle. Isso não exclui componentes transitivos do build.
- Open Sans: cópias de aviso Apache-2.0 em app-web/fonts/LICENSE.txt e dist/fonts/LICENSE.txt. Preservar licença e créditos no APK e na página de instalação.
- O aplicativo abre o serviço Meu Ritmo, que tem inventário de dependências próprio.
- APK distribuído: a configuração de assinatura do Gradle não comprova o certificado de um arquivo específico. Antes de distribuir, verificar com apksigner verify --print-certs e conferir assets contidos no APK. Não confundir assinatura Android com a assinatura Git.
- Pendência: resolver árvore Gradle e revisar APK efetivamente entregue; não foi possível inspecionar os binários nesta revisão.

A tag autoria-2026-11-26 foi consultada na API GitHub: assinatura válida (verified=true, reason=valid), apontando para a versão documentada em EVIDENCIA-2026-11-26.md. A auditoria atual é posterior à versão assinada; seu commit não foi assinado com a chave pessoal do mantenedor.
