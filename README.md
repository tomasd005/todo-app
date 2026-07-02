# To-do List em Java

Este projeto define uma arquitetura inicial em Java para um aplicativo de gestão de tarefas, organizada em módulos para separar responsabilidades e facilitar evolução.

## Estrutura proposta

- todo-domain: entidades, enums e contratos de repositório
- todo-application: serviços de aplicação e regras de negócio
- todo-infrastructure: implementações concretas de repositórios e integrações
- todo-api: camada de entrada para interação com o sistema

## Próximos passos

1. Implementar persistência real com banco de dados
2. Adicionar autenticação completa
3. Criar interfaces web ou console mais ricas
4. Evoluir para um fluxo de Kanban, calendário e alertas
