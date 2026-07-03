workspace "Subscription Tracker" "A personal finance tool to track software subscriptions and calculate monthly spending." {
    model {
        user = person "User" "An individual who tracks their recurring subscription costs and monitors upcoming renewal dates."
        
        subscriptionTracker = softwareSystem "Subscription Tracker" "Centralizes software subscription data, calculates monthly spending, and tracks renewal metrics." {
            frontend = container "Frontend Web Application" "Provides the user interface for viewing, editing, and managing personal software subscriptions." "Angular"
            backend = container "Backend API" "Handles subscription CRUD operations, financial calculations, and exposes a RESTful API." "Java and Spring Boot" {
                controller = component "Subscription Controller" "Handles HTTP requests."
                service = component "Calculation Service" "Calculates metrics."
                dao = component "Subscription DAO" "Queries database using Spring JDBC"
            }
            database = container "Database" "Stores user profiles, subscription details, and renewal logs in a relational schema." "PostgreSQL" "Database" 
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