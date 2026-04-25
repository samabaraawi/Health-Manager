# 🧠 Health Manager 

## 🚀 About the Project

Health Manager is a Java-based application designed to manage patient records, medical conditions, treatments, medications, and symptoms.

This project focuses on implementing complex relationships between entities using **custom data structures**, specifically a **Circular Doubly Linked List**, instead of built-in collections.

The system simulates a real-world healthcare management environment, ensuring that treatments and medications are assigned correctly based on each patient's condition.

---

## 💡 Key Features

* 👤 Patient management (add, update, delete, search)
* ⚕️ Condition management with recommended treatments & medications
* 💉 Treatment tracking with validation rules
* 💊 Medication management with condition-based restrictions
* 🤒 Symptom tracking per patient
* 📂 Load data from structured text files
* 📊 Generate reports across patients, conditions, and treatments
* 🎛️ Interactive JavaFX GUI with multiple tabs and dashboard

---

## 🧱 Core Data Structure

The system is built using a **custom Circular Doubly Linked List** implementation:

* Supports insertion, deletion, traversal, and search
* Optimized traversal (from head or tail based on index)
* Used to manage:

  * Patients
  * Conditions
  * Treatments
  * Medications
  * Symptoms

This approach ensures a deep understanding of memory-based data structures and pointer manipulation.

---

## 🏗️ System Design

The application is modular and organized into multiple components:

* **Custom Data Structures**

  * `CircularDoublyLinkedList`
  * `Node`

* **Core Models**

  * `Patient`
  * `Condition`
  * `Treatment`
  * `Medication`
  * `Symptom`

* **Managers (Logic Layer)**

  * `PatientManager`
  * `ConditionManager`

* **UI Components (JavaFX Tabs)**

  * Patients
  * Conditions
  * Treatments
  * Medications
  * Symptoms
  * Reports

---

## 🧠 Data Integrity & Validation

The system enforces strict validation rules:

* Treatments must match the patient's condition
* Medications must be recommended for the condition
* Invalid assignments are rejected

This ensures realistic and consistent healthcare data modeling.

---

## ▶️ How to Run

1. Open the project in IntelliJ IDEA
2. Ensure JavaFX is properly configured
3. Run `MainApp.java`
4. Load input files:

   * `conditions.txt`
   * `patients.txt`
   * `treatments.txt`
   * `medications.txt`

---

## 📄 Input File Format

Example:

```id="v3c9os"
ConditionName|Treatment1,Treatment2|Medication1,Medication2
Diabetes|Insulin Therapy,Diet Counseling|Insulin,Metformin
```

---

## 👩‍💻 Author

**Sama Baraawi**
Computer Science Student

---
This project demonstrates strong understanding of **data structures, system design, and problem-solving**, showcasing the ability to build complex applications without relying on built-in libraries.
