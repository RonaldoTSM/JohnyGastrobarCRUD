# JohnyGastrobarCRUD

Projeto de banco de dados desenvolvido para o módulo 1 da disciplina, integrando um sistema de gerenciamento para o restaurante fictício **Johny Gastrobar**. O sistema implementa um CRUD completo da entidade `Funcionario`, com especializações e interface gráfica desenvolvida em JavaFX.

---

## 📌 Funcionalidades

- ✅ Inserção de Funcionários (Garçom, Cozinheiro, Bartender, Gerente)
- ✅ Listagem em tabela com JavaFX
- ✅ Atualização de salário
- ✅ Exclusão com `ON DELETE CASCADE`
- ✅ Interface intuitiva com JavaFX
- ✅ Conexão direta via JDBC (sem ORM)

---

## 🗂 Estrutura do Projeto

- `Funcionario.java`: classe modelo
- `FuncionarioDAO.java`: lógica de acesso ao banco (SQL via JDBC)
- `ConnectionFactory.java`: central de conexão com o MySQL
- `MainFX.java`: interface gráfica com JavaFX
- `App.java`: versão inicial em terminal (console)
- `pom.xml`: dependências gerenciadas via Maven

---

## 🛠 Tecnologias

- Java 22
- JavaFX (frontend)
- MySQL
- JDBC
- Maven

---

## 💾 Banco de Dados

O banco `johny_gastrobar` foi projetado com tabelas para:

- Funcionários com especializações (`Garcom`, `Cozinheiro`, `Bartender`, `Gerente`)
- Mesas, Pedidos, Itens e Pagamentos
- Controle de pedidos com múltiplos itens
- Relacionamentos com **`ON DELETE CASCADE`** e **`AUTO_INCREMENT`**

Exemplo de criação de tabela:
```sql
CREATE TABLE Funcionario (
  id_funcionario INT PRIMARY KEY AUTO_INCREMENT,
  nome VARCHAR(100),
  cpf VARCHAR(11) UNIQUE,
  salario DECIMAL(10,2),
  data_contratacao DATE
);
