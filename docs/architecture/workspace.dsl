workspace "Subscription Tracker" "A personal finance tool to track software subscriptions and calculate monthly spending." {
    model {
        user = person "User" "An individual who tracks their recurring subscription costs and monitors upcoming renewal dates."
        
        subscriptionTracker = softwareSystem "Subscription Tracker" "Tracks software subscriptions and their prices, and calculates total and monthly spending." {
        frontend = container "Frontend Web Application" "Provides the user interface for viewing, adding, editing, and deleting subscriptions." "Angular"
        backend = container "Backend API" "Handles subscription CRUD operations, calculates spending totals, and exposes a RESTful API." "Java and Spring Boot" {
            controller = component "Subscription Controller" "Exposes REST endpoints; handles HTTP requests, responses, and status codes."
            service = component "Subscription Service" "Holds business logic; validates input, maps between DTOs and entities, and interprets DAO results."
            dao = component "Subscription DAO" "Executes hand-written SQL against the database using Spring JdbcTemplate."
        }
        database = container "Database" "Stores subscription records (name, price)." "PostgreSQL" "Database"
}

        user -> frontend "Views and edits subscription data using" "HTTPS"
        frontend -> controller "Makes API calls to" "JSON/HTTPS"
        controller -> service "Delegates business logic execution to"
        service -> dao "Calls data access method on"
        dao -> database "Queries and updates subscription data using" "JDBC/SQL"
    }

    views {
        systemContext subscriptionTracker "SystemContext" {
            include *
            autolayout lr
        }

        container subscriptionTracker "Containers" {
            include *
            autolayout lr
        }

        component backend "Component" {
            include *
            autolayout lr
        }

        dynamic backend "CreateSubscription" "Flow for creating a subscription" {
            controller -> service "Validates request and maps to entity"
            service -> dao "Saves the subscription"
            dao -> database "INSERT ... RETURNING id, name, price"
            database -> dao "Returns saved row"
            dao -> service "Returns Subscription"
            service -> controller "Returns SubscriptionResponse"
        }

        dynamic backend "GetAllSubscriptions" "Flow for fetching all subscriptions" {
            controller -> service "Requests all subscriptions"
            service -> dao "Fetches all subscriptions"
            dao -> database "SELECT id, name, price FROM subscriptions"
            database -> dao "Returns rows"
            dao -> service "Returns List<Subscription>"
            service -> controller "Returns List<SubscriptionResponse>"
        }

        dynamic backend "GetSubscriptionById" "Flow for fetching a single subscription by id" {
            controller -> service "Requests the subscription by id"
            service -> dao "Fetches the subscription by id"
            dao -> database "SELECT id, name, price FROM subscriptions WHERE id = ?"
            database -> dao "Returns the matching row"
            dao -> service "Returns Optional<Subscription>"
            service -> controller "Returns SubscriptionResponse"
        }

        dynamic backend "UpdateSubscription" "Flow for updating an existing subscription" {
            controller -> service "Validates request and maps to entity"
            service -> dao "Updates the subscription"
            dao -> database "UPDATE subscriptions SET name = ?, price = ? WHERE id = ?"
            database -> dao "Returns rows affected"
            dao -> service "Returns rows-updated count"
            service -> controller "Returns SubscriptionResponse built from the request"
        }

        dynamic backend "DeleteSubscription" "Flow for deleting a subscription by id" {
            controller -> service "Requests deletion by id"
            service -> dao "Deletes the subscription"
            dao -> database "DELETE FROM subscriptions WHERE id = ?"
            database -> dao "Returns rows affected"
            dao -> service "Returns rows-deleted count"
            service -> controller "Confirms deletion (204 No Content)"
        }

        dynamic backend "CalculateTotal" "Flow for calculating total monthly spending" {
            controller -> service "Requests the total monthly amount"
            service -> dao "Requests the sum of all prices"
            dao -> database "SELECT COALESCE(SUM(price), 0) FROM subscriptions"
            database -> dao "Returns the summed total"
            dao -> service "Returns BigDecimal"
            service -> controller "Returns TotalAmountResponse"
        }

        dynamic backend "CountSubscriptions" "Flow for counting subscriptions" {
            controller -> service "Requests the subscription count"
            service -> dao "Requests the number of subscriptions"
            dao -> database "SELECT COUNT(*) FROM subscriptions"
            database -> dao "Returns the count"
            dao -> service "Returns long"
            service -> controller "Returns SubscriptionCountResponse"
        }

        dynamic backend "GetSubscriptionByIdNotFound" "Error flow: requested subscription does not exist" {
            controller -> service "Requests the subscription by id"
            service -> dao "Fetches the subscription by id"
            dao -> database "SELECT id, name, price FROM subscriptions WHERE id = ?"
            database -> dao "Returns no rows"
            dao -> service "Returns empty Optional<Subscription>"
            service -> controller "Throws SubscriptionNotFoundException; responds 404 Not Found"
        }

        dynamic backend "CreateSubscriptionDuplicateName" "Error flow: subscription name already exists" {
            controller -> service "Validates request and maps to entity"
            service -> dao "Saves the subscription"
            dao -> database "INSERT INTO subscriptions (name, price) VALUES (?, ?) RETURNING id, name, price"
            database -> dao "Rejects the insert: duplicate name violates the unique index"
            dao -> service "Translates the violation and throws SubscriptionAlreadyExistsException"
            service -> controller "Propagates the exception; responds 409 Conflict"
        }

        dynamic backend "CreateSubscriptionValidationFailure" "Error flow: request fails validation at the boundary" {
            frontend -> controller "Submits a subscription with invalid data"
            controller -> frontend "Rejects with 400 Bad Request; the request never reaches the service"
        }

        styles {
            element "Person" {
                background #08427b
                color #ffffff
                shape Person
            }
            element "Software System" {
                background #1168bd
                color #ffffff 
            }    
            element "Container" {
                background #438dd5
                color #ffffff
            }
            element "Component" {
                background #85bbf0
                color #000000
            }
            element "Database" {
                shape Cylinder
            }
        }
    }
}