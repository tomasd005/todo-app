# Arquitetura proposta do aplicativo de tarefas

## Visão geral

A aplicação foi organizada em módulos independentes para separar responsabilidades e facilitar a evolução.

## Módulos

- todo-domain
  - Entidades principais: Task, Project, Subtask
  - Enums: Priority, TaskStatus
  - Interfaces de repositório

- todo-application
  - Serviços de aplicação: TaskService, ProjectService
  - Regras de negócio e orquestração de casos de uso

- todo-infrastructure
  - Implementações concretas de repositórios
  - Integrações com banco de dados, APIs externas e mecanismos de persistência

- todo-api
  - Ponto de entrada da aplicação
  - Futuramente poderá expor REST, CLI ou interface web

## Fluxo de funcionamento

1. O usuário interage com a camada de API.
2. A API chama os serviços de aplicação.
3. Os serviços usam o domínio e os repositórios.
4. A infraestrutura implementa a persistência.

## Princípios adotados

- Separação de responsabilidades por camada
- Domínio rico, com entidades e regras centrais
- Dependências direcionadas para dentro
- Facilitação de testes unitários

## Próximas etapas

- Adicionar autenticação de usuários
- Implementar persistência com banco de dados
- Criar endpoints para CRUD de tarefas e projetos
- Introduzir visualizações em lista, calendário e Kanban
