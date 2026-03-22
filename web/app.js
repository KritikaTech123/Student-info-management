const form = document.getElementById("studentForm");
const formTitle = document.getElementById("formTitle");
const studentIdInput = document.getElementById("studentId");
const enrollmentNoInput = document.getElementById("enrollmentNo");
const nameInput = document.getElementById("name");
const ageInput = document.getElementById("age");
const departmentInput = document.getElementById("department");
const cgpaInput = document.getElementById("cgpa");
const tableWrap = document.getElementById("tableWrap");
const searchBox = document.getElementById("searchBox");
const cancelBtn = document.getElementById("cancelBtn");

const STORAGE_KEY = "studentInfoManagement.students";
const NEXT_ID_KEY = "studentInfoManagement.nextId";

let students = [];

function loadStudents() {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) {
        students = [];
        return;
    }

    try {
        const parsed = JSON.parse(raw);
        students = Array.isArray(parsed) ? parsed : [];
    } catch {
        students = [];
    }
}

function saveStudents() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(students));
}

function getNextId() {
    const raw = localStorage.getItem(NEXT_ID_KEY);
    const value = Number(raw);
    return Number.isInteger(value) && value > 0 ? value : 1;
}

function setNextId(nextId) {
    localStorage.setItem(NEXT_ID_KEY, String(nextId));
}

function validateInputs() {
    const name = nameInput.value.trim();
    const age = Number(ageInput.value.trim());
    const enrollmentNo = enrollmentNoInput.value.trim();
    const department = departmentInput.value.trim();
    const cgpa = Number(cgpaInput.value.trim());

    if (!name) {
        return "Name is required";
    }
    if (!Number.isInteger(age) || age < 1 || age > 120) {
        return "Age must be a whole number between 1 and 120";
    }
    if (!/^[0-9]+$/.test(enrollmentNo)) {
        return "Enrollment number must contain only digits";
    }
    if (/\d/.test(department)) {
        return "Department cannot contain numbers";
    }
    if (!Number.isFinite(cgpa) || cgpa < 0 || cgpa > 10) {
        return "CGPA must be between 0.00 and 10.00";
    }

    return null;
}

function renderTable() {
    const term = searchBox.value.trim().toLowerCase();
    const filtered = students.filter((s) =>
        s.name.toLowerCase().includes(term) ||
        s.department.toLowerCase().includes(term) ||
        String(s.enrollmentNo).includes(term)
    );

    if (!filtered.length) {
        tableWrap.innerHTML = "<p>No students found.</p>";
        return;
    }

    const rows = filtered.map((s) => `
        <tr>
            <td>${s.id}</td>
            <td>${escapeHtml(s.name)}</td>
            <td>${s.age}</td>
            <td>${escapeHtml(s.enrollmentNo)}</td>
            <td>${escapeHtml(s.department)}</td>
            <td>${Number(s.cgpa).toFixed(2)}</td>
            <td>
                <div class="row-actions">
                    <button class="small secondary" onclick="startEdit(${s.id})">Edit</button>
                    <button class="small" onclick="deleteStudent(${s.id})">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");

    tableWrap.innerHTML = `
        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Age</th>
                    <th>Enrollment No.</th>
                    <th>Department</th>
                    <th>CGPA</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody>${rows}</tbody>
        </table>
    `;
}

function formDataFromInputs() {
    return {
        name: nameInput.value.trim(),
        age: Number(ageInput.value.trim()),
        enrollmentNo: enrollmentNoInput.value.trim(),
        department: departmentInput.value.trim(),
        cgpa: Number(cgpaInput.value.trim()),
    };
}

form.addEventListener("submit", (event) => {
    event.preventDefault();

    const validationError = validateInputs();
    if (validationError) {
        alert(validationError);
        return;
    }

    const id = studentIdInput.value;
    const payload = formDataFromInputs();

    if (id) {
        const idx = students.findIndex((s) => s.id === Number(id));
        if (idx === -1) {
            alert("Student not found");
            return;
        }

        students[idx] = {
            id: Number(id),
            ...payload,
        };
    } else {
        const nextId = getNextId();
        students.push({
            id: nextId,
            ...payload,
        });
        setNextId(nextId + 1);
    }

    saveStudents();
    resetForm();
    renderTable();
});

function deleteStudent(id) {
    const ok = confirm("Delete this student record?");
    if (!ok) {
        return;
    }
    students = students.filter((s) => s.id !== id);
    saveStudents();
    renderTable();
}

function startEdit(id) {
    const student = students.find((s) => s.id === id);
    if (!student) {
        return;
    }
    formTitle.textContent = "Edit Student";
    cancelBtn.hidden = false;
    studentIdInput.value = student.id;
    enrollmentNoInput.value = student.enrollmentNo;
    nameInput.value = student.name;
    ageInput.value = student.age;
    departmentInput.value = student.department;
    cgpaInput.value = student.cgpa;
}

function resetForm() {
    form.reset();
    formTitle.textContent = "Add Student";
    cancelBtn.hidden = true;
    studentIdInput.value = "";
}

cancelBtn.addEventListener("click", resetForm);
searchBox.addEventListener("input", renderTable);

function escapeHtml(value) {
    return value
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

window.startEdit = startEdit;
window.deleteStudent = deleteStudent;

loadStudents();
renderTable();
