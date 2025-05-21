-- string1 유저의 강의 parsed_text 업데이트
UPDATE app.lectures
SET parsed_text = '{
  "total_pages": 12,
  "pages": [
    {"page_number": 1, "text": "재귀 함수는 자기 자신을 호출하는 함수이다. 이 함수는 기저 사례를 갖고 있어야 한다."},
    {"page_number": 2, "text": "반복문과 재귀 함수는 모두 동일한 결과를 낼 수 있지만, 재귀는 더 많은 메모리를 사용할 수 있다."},
    {"page_number": 3, "text": "기저 사례는 재귀 함수가 종료되는 조건이다. 기저 사례가 없으면 무한 재귀에 빠질 수 있다."},
    {"page_number": 4, "text": "반복문은 상태를 유지하기 위해 변수를 사용하지만, 재귀는 함수 호출 스택을 사용한다."},
    {"page_number": 5, "text": "재귀 함수는 코드가 간결해질 수 있지만, 성능이 떨어질 수 있다."},
    {"page_number": 6, "text": "재귀 함수는 주로 트리 구조를 탐색하는 데 유용하다."},
    {"page_number": 7, "text": "반복문은 주로 순차적인 작업에 적합하다."},
    {"page_number": 8, "text": "재귀 함수는 메모리 사용량이 많아질 수 있지만, 코드가 더 읽기 쉬울 수 있다."},
    {"page_number": 9, "text": "반복문은 메모리 사용량이 적지만, 코드가 복잡해질 수 있다."},
    {"page_number": 10, "text": "재귀 함수는 기저 사례를 통해 종료되며, 반복문은 조건문을 통해 종료된다."},
    {"page_number": 11, "text": "재귀 함수는 스택 오버플로우를 유발할 수 있다."},
    {"page_number": 12, "text": "반복문은 스택 오버플로우를 유발하지 않는다."}
  ]
}'
WHERE course_id = '1bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
  AND user_id = (SELECT id FROM app.users WHERE name = 'string1');

-- string2 유저의 강의 parsed_text 업데이트
UPDATE app.lectures
SET parsed_text = '{
  "total_pages": 6,
  "pages": [
    {
      "page_number": 1,
      "text": "Has Winter Come to Samsung?"
    },
    {
      "page_number": 2,
      "text": "Samsung Electronics'''' stock price, which stood at 83,100 won on August 1, plunged by nearly 40 percent to 49,900 won by November 14—the lowest price since May 2020. On October 8, Jeon Young-hyun, the head of Samsung Electronics'''' Device Solution (DS) division apologized for an \"Earning Shock\" as preliminary result of the third quarter of this year fell well below market expectations. The apology included a commitment to rebuild and address the \"technological competitiveness\" and \"organizational culture\" issues."
    },
    {
      "page_number": 3,
      "text": "Nonetheless, Samsung has maintained its position as the No. 1 player in the global Dynamic Random-Access Memory (DRAM) market for over 30 years, commanding a 41.1 percent share as of the third quarter of 2024. However, why is Samsung Electronics facing winter? In this article, The Ajou Globe (The AG) focuses on Samsung''''s current technological competitiveness in the semiconductor market explaining key terms."
    },
    {
      "page_number": 4,
      "text": "First, what is DRAM?  Key term 1: Memory semiconductor and non-memory semiconductor. Semiconductors are materials that function as a conductor and insulator under specific conditions. They are categorized into memory and non-memory semiconductors. Memory semiconductors include DRAM, High-Bandwidth Memory (HBM), and many more. DRAM is a volatile memory device that loses stored data when power is cut off. It is widely used in computers for its large capacity and high speed. However, these days, in the age of Artificial Intelligence (AI), the most popular memory semiconductor is \"HBM,\" not DRAM. Meanwhile, non-memory semiconductors include Graphics Processing Unit (GPU), Central Processing Unit (CPU), and many more. CPU interprets program instructions, controls the operation of the computer and handles arithmetic and logical operations. GPU is a computing device for graphics and is currently the most popular non-memory semiconductor. Because, unlike the CPU''''s serial processing the GPU uses parallel processing, which is ideal for high-speed data processing required for AI training."
    },
    {
      "page_number": 5,
      "text": "Why is HBM more popular than DRAM nowadays?"
    },
    {
      "page_number": 6,
      "text": "Key term 2: HBM. The harmony between memory semiconductor''''s information storage and non-memory semiconductor''''s computational capabilities is important. Unfortunately, DRAM (memory semiconductor) cannot keep pace with GPU (non-memory semiconductor)''''s data processing speed. Therefore, in 2013, SK Hynix developed the first HBM, which stores data while matching GPU''''s speed by stacking multiple DRAM layers and creating passages between them to accelerate processing."
    }
  ]
}'
WHERE course_id = '2bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
  AND user_id = (SELECT id FROM app.users WHERE name = 'string2');

-- string3 유저의 강의 parsed_text 업데이트
UPDATE app.lectures
SET parsed_text = '{
  "total_pages": 51,
  "pages": [
    {
      "page_number": 1,
      "text": "Operating Systems: An Overview\nInstructor: Dr. Jiwoong Park"
    },
    {
      "page_number": 2,
      "text": "Course Schedule\n기존 강의 일정\n주\n차\n강의 내용\n9\nIntroduction to Operating System\n10\nLinux OS Structures\n11\n변경 강의 일정\n주\n차\n강의 내용\n9\nIntroduction to Operating System\n10\nLinux OS Structures\nPractice for Raspberry Pi\n11\nPractice for Raspberry Pi\n12\nI/O Operations\n12\nInter-Process Communication\n과제 #3\n13\nProcess & Thread Management\n13\n휴강 (5/28, 5/30 해외 출장)\nProcess & Thread Management\n보강\n14\nInter-Process Communication\n14\nI/O Systems and Operations\n15\nSpecial Topics on Embedded OS\n15\n특강\n16\nFinal Exam\n16\n비고\n과제 #2\n과제 #3\n비고\n과제 #2\n과제 #4\n2"
    },
    {
      "page_number": 3,
      "text": "Grading Policy\n기존\n수정\n출석\n10%\n10%\n중간고사\n25%\n25%\n기말고사\n25%\n0%\n퀴즈\n15%\n15%\n과제\n25%\n50%\n• 퀴즈 #1 (4), 퀴즈 #2 (11)\n• 과제 #1 (15), 과제 #2(5), 과제 #3(10), 과제 #4(20)\n– 과제 #4는 기말 대체 과제\n3"
    },
    {
      "page_number": 4,
      "text": "Practice with Raspberry Pi\n• Raspberry Pi\n– Originally, the Raspberry Pi project was created with the promotion of\nteaching basic computer science in schools in mind, leading to low cost,\nmodularity, and open design. (Wikipedia)\n• Programming with RBP\n– Linux system calls\n– Kernel module (I/O, Sensors)\n– Team Assignment (#4)\n4"
    },
    {
      "page_number": 5,
      "text": "References\n• [Textbook] Abraham Silberschatz, Peter Baer Galvin, and Greg Gagne,\n“Operating System Concepts (10th Edition) – Chapters 1 & 2,” Wiley\n2019."
    },
    {
      "page_number": 6,
      "text": "https://galvin.info/history-of-operatingsystem-concepts-textbook/\n5"
    },
    {
      "page_number": 7,
      "text": "Contents\n• What is an Operating System (OS)?\n• OS Services and Operations\n• System Calls\n6"
    },
    {
      "page_number": 8,
      "text": "What is an Operating System?\n• A program that acts as an intermediary between applications and\ncomputer hardware\n1) Users\n- People, machines, other computers\n- Want to solve computing problems\n2) Programs\n- Solve the computing problems of\nthe users using computer resources\n4) Hardware\n- Provides basic computing resources\n- CPU, memory, I/O devices\n7"
    },
    {
      "page_number": 9,
      "text": "What is an Operating System?\n• A program that acts as an intermediary between applications and\ncomputer hardware\n1) Users\n- People, machines, other computers\n- Want to solve computing problems\n2) Programs\n- Solve the computing problems of\nthe users using computer resources\n3) Operating system\n- Controls and coordinates use of\nhardware among various\napplications and users\n4) Hardware\n- Provides basic computing resources\n- CPU, memory, I/O devices\n8"
    },
    {
      "page_number": 10,
      "text": "What is an Operating System?\n• No universally accepted definition\n• “Software that converts hardware into a useful form for applications”\n• “The one program so-called kernel running at all times on the computer”\n– Everything else is either\n System programs (ships with the operating system)\n Or application programs\n9"
    },
    {
      "page_number": 11,
      "text": "Goals of Operating Systems\n• Provide an environment in which a user can execute user programs and\nmake solving user problems easier\n• Make the computer system convenient to use\n• Use computer hardware in an efficient and safe manner\n10"
    },
    {
      "page_number": 12,
      "text": "What Operating Systems Do\n• Application/user view\n– Want convenience, ease of use, and good performance\n They don’t care about resource utilization\n– Provides an execution environment for running programs\n– Provides an abstract view of the underlying computer system\n Processors  Processes, threads\n Memory  Virtual memory address spaces\n Storage  Files, directories\n I/O devices  Files (ioctls)\n Networks  Files (sockets, pipes, …)\n11"
    },
    {
      "page_number": 13,
      "text": "What Operating Systems Do\n• System view\n– Deal with many applications and users that compete with each other for\nresource use\n … and make all of them happy\n– Resource manager\n Manages various resources of a computer system\n» CPU, Memory, I/O devices, …\n Decides between conflicting requests for efficient and fair resource use\n– Control program\n Controls execution of programs to prevent errors and improper use of the\ncomputer\n12"
    },
    {
      "page_number": 14,
      "text": "Operating System Services\n• OS services provide functions that are helpful to the user:\n– User interface\n Varies between Command-Line Interface (CLI), Graphics User Interface (GUI), etc.\n– Program execution\n Load a program into memory and run the program\n End execution, either normally or abnormally (indicating error)\n– I/O operations\n A running program may require I/O, which may involve a file or an I/O device\n– File-system manipulation\n Programs need to create, delete, read, and write files and directories\n Search, list file information, permission management, …\n13"
    },
    {
      "page_number": 15,
      "text": "Operating System Services\n– Communications\n Processes may exchange information, on the same computer or between\ncomputers over a network\n» Via shared memory or through message passing (packets moved by the OS)\n– Error detection\n Constantly aware of possible errors\n» May occur in the CPU and memory hardware, in I/O devices, in user program\n» Take an appropriate action to ensure correct and consistent computing\n» Debugging facilities can greatly enhance the user’s and programmer’s abilities to efficiently\nuse the system\n14"
    },
    {
      "page_number": 16,
      "text": "Operating System Services\n• Another set of OS functions exists for ensuring the efficient operation of\nthe system itself via resource sharing\n– Resource allocation\n When multiple users or multiple jobs run concurrently, resources must be\nallocated to each of them\n Many types of resources - CPU cycles, main memory, file storage, I/O devices\n– Accounting\n Keep track of which users use how much and what kinds of computer resources\n– Protection and security\n Protection involves ensuring that all access to system resources is controlled\n Security of the system from outsiders requires user authentication, and extends to\ndefending external I/O devices from invalid access attempts\n15"
    },
    {
      "page_number": 17,
      "text": "How Computer Systems Work\n16"
    },
    {
      "page_number": 18,
      "text": "Computer System Organization\n• One or more CPUs and device controllers are connected through a\ncommon bus providing access to the main memory\n• Concurrent execution of CPUs and devices compete for memory cycles\nBus\n17"
    },
    {
      "page_number": 19,
      "text": "Computer System Operation\n• Each device controller is in charge of a particular device type\n• Each device controller has a local buffer\n• I/O is data transfer from the I/O device to the controller or vice versa\n• CPU moves data from the controller to the main memory or vice versa\nBus\n18"
    },
    {
      "page_number": 20,
      "text": "Programmed I/O (a.k.a. polling)\n• CPU repeatedly checks the state of an I/O device through a device\ncontroller\n– e.g., whether the requested I/O operation is completed…\n• If its state is ready, CPU directly moves data required for an I/O\noperation\n• CPU must always be involved in I/O operation handling  CPU\nresource can be used inefficiently\n– CPU may waste substantial time waiting for I/O devices to be ready (busy\nwaiting)\n19"
    },
    {
      "page_number": 21,
      "text": "Interrupt-driven I/O\n• Device controller notifies CPU of events by generating an interrupt\n– When an incoming event occurs in an I/O device\n– Or when a device controller has finished I/O operation\nInterrupt\nBus\n20"
    },
    {
      "page_number": 22,
      "text": "Interrupt Handling Process\n1. OS preserves the current state of the\nCPU\n– To come back after handling the interrupt\n– Save registers and the address of the\ninterrupted instruction to the memory\n2. Jumps to a designated interrupt\nservice routine (called interrupt\nhandler)\n– Interrupt vector contains the addresses\nof all the service routines\nFlow of interrupt handling\n3. Return to the last state by restoring the\nregisters\n21"
    },
    {
      "page_number": 23,
      "text": "Operating System Operations\n• An operating system is interrupt-driven\n– Hardware interrupt generated by hardware devices (I/O devices)\n Asynchronous event  independent on a currently-executing process\n– Software interrupt generated by executing instructions\n Synchronous event  always generated by a currently-executing process\n Software error (e.g., division by zero), request for operating system service, etc.\n\nInterrupt timeline\n22"
    },
    {
      "page_number": 24,
      "text": "Interrupt-driven I/O\n• More efficient than programmed I/O\n– Because CPU doesn’t have to do busy waiting\n• However, CPU burden increases when moving a large amount of\ndata…\n– CPU can still be used inefficiently\nLarge\ndata\nBus\n23"
    },
    {
      "page_number": 25,
      "text": "Direct Memory Access (DMA) I/O\n• Uses a specific hardware for handling I/O operation\n– called DMA controller\n• CPU requests an I/O operation to DMA controller\n• DMA controller performs data transfer between DC and main memory\nwithout CPU intervention\n• DMA controller notifies CPU of I/O operation completion by generating\nan interrupt\n• Benefits\n– CPU can perform other tasks while an I/O operation is being processed\n– Used for high-speed I/O devices to transmit information at close to memory\nspeeds\n24"
    },
    {
      "page_number": 26,
      "text": "DMA I/O: read operation\ndma는 비싸서 많이 안 쓰고\ninterrupt driven은 user program 제어,\npolling은 센서 제어에 많이 쓴다.\n\n25"
    },
    {
      "page_number": 27,
      "text": "DMA I/O: write operation\n26"
    },
    {
      "page_number": 28,
      "text": "Multiprogramming\n• One of the most important aspects of OS is the ability to run multiple\nprograms\n• Multiprogramming needed for efficiency\n– A single program cannot keep either CPU or I/O\ndevices busy at all times\n– Multiprogramming organizes jobs (code and data) so\nthat CPU always has one to execute (= never idle)\n– A subset of total jobs in system is kept in memory\n– One job selected and run via job scheduling\n– When it has to wait for I/O, OS switches to another job\nMemory Layout for\nMultiprogrammed System\n27"
    },
    {
      "page_number": 29,
      "text": "Multitasking\n• Multitasking (Time sharing) is a logical extension of multiprogramming\nin which CPU switches jobs frequently so that users can interact with\neach running job, creating interactive computing\n– If several jobs are ready to run at the same time  CPU scheduling\n Each job is given a time slice\n Run a job for a time slice and then switches to the next job\n– Response time should be < 1 second\nJob 6\nJob 5\n10ms\n10ms\n10ms (time slice size)\nJob 1\nJob 4\n10ms\n10ms\nJob 3\n10ms\nJob 2\n28"
    },
    {
      "page_number": 30,
      "text": "Systems Calls\n29"
    },
    {
      "page_number": 31,
      "text": "Recap the last session\n• How does a computer operate?\n– Device controllers notify systems of I/O completion with interrupts\n– OS handles the interrupts and restores the system to its last interrupted state\nInterrupt\n30"
    },
    {
      "page_number": 32,
      "text": "Protecting the System\n• How can OS prevent user applications from harming the system?\n– What if an application accesses disk drive directly?\n– What if an application overrides interrupt handlers for keyboard?\n– What if an application executes the HLT instruction?\n\n31"
    },
    {
      "page_number": 33,
      "text": "Protecting the System\n• Dual-mode operation: CPU operates in “user mode” or “kernel mode”\n– Allows OS to protect itself and other system components\n Separate critical things from general things\n– Mode bit (provided by hardware) allows to distinguish on which mode the\nsystem is running (0: kernel mode, 1: user mode)\n– All privileged instructions are only executable in the kernel mode\n E.g., HLT in x86\n• Modern CPUs support more than two\nmodes (i.e. multi-mode operations)\n– i.e. virtual machine manager (VMM)\nmode for guest VMs\n– VMs should be limited to its space for\nmemory access\nSingle OS\nOS Virtualization"
    },
    {
      "page_number": 34,
      "text": "Privileged Instructions\n• A set of instructions that should be executed carefully\n– Direct I/O access\n E.g., IN/OUT instructions in IA-32\n– Accessing/manipulating system registers\n Control registers\n Interrupt service routine table\n– Memory state management\n Page table updates, page table pointers, TLB loads, etc\n– HLT instruction in IA-32\n• Executable in the kernel mode\n– May generate an exception (fault) if an application tries to run a privileged\ninstruction in user mode\n33"
    },
    {
      "page_number": 35,
      "text": "Interrupt vs. Exception\n• Exception (= Software interrupt)\n– Generated by executing instructions\n Software error (e.g., division by zero), unauthorized access to data, request for\noperating system service, …\n– Synchronous\n Happens when CPU executes an instruction\n– Trap(expected, intended) or fault(unexpected)\n– Handled like interrupts\n• C.f., interrupt (= Hardware interrupt)\n– Generated by hardware devices\n– Occurs asynchronously (at any time)\n• Modern OSes are interrupt(including exception)-driven\n– The transition from user mode to kernel mode is done via interrupt or\nexception\n34"
    },
    {
      "page_number": 36,
      "text": "Transition from User to Kernel Mode\n• By interrupt\n– Asynchronous\n– E.g., Timer\n In the kernel mode, OS set a timer to generate an interrupt after some time period\n(privileged instruction)\n In the user mode, an application process keeps using the CPU\n The timer generates a timer interrupt when the timer expires\n» Switched to the kernel mode\n The OS can make a decision on which process will be executed next\n35"
    },
    {
      "page_number": 37,
      "text": "Transition from User to Kernel Mode\n• By system call\n– Synchronous\n36"
    },
    {
      "page_number": 38,
      "text": "System Calls\n• Programming interface to the services provided by the OS\n• Typically written in a high-level language (C/C++)\n• Mostly accessed by programs via a high-level Application Programming\nInterface (API) rather than direct system call use\n• Most common APIs\n– Win32 API for Windows variants\n– POSIX API for POSIX-based systems\n Virtually include all versions of UNIX, Linux, and Mac OSX\n– Java API for the Java virtual machine (JVM)\n37"
    },
    {
      "page_number": 39,
      "text": "A View of Operating System Services\n38"
    },
    {
      "page_number": 40,
      "text": "Types of System Calls\n• Process control\n– create process, terminate process\n– end, abort\n– load, execute\n– get process attributes, set process attributes\n– wait for time\n– wait event, signal event\n– allocate and free memory\n– dump memory if error\n– debugger for determining bugs, single step execution\n– locks for managing access to shared data between processes\n39"
    },
    {
      "page_number": 41,
      "text": "Types of System Calls\n• File management\n– create file, delete file\n– open, close file\n– read, write, reposition\n– get and set file attributes\n• Device management\n– request device, release device\n– read, write, reposition\n– get device attributes, set device attributes\n– logically attach or detach devices\n40"
    },
    {
      "page_number": 42,
      "text": "Types of System Calls\n• Information maintenance\n– get time or date, set time or date\n– get system data, set system data\n– get and set process, file, or device attributes\n• Communications\n– create, delete communication connection\n– send, receive messages (message passing model)\n– create and gain access to memory regions (shared memory model)\n– transfer status information\n– attach and detach remote devices\n41"
    },
    {
      "page_number": 43,
      "text": "Types of System Calls\n• Protection\n– Control access to resources\n– Get and set permissions\n– Allow and deny user access\n42"
    },
    {
      "page_number": 44,
      "text": "Example of System Calls\n• System call sequence to copy the contents of one to another file\n43"
    },
    {
      "page_number": 45,
      "text": "Windows and UNIX System Calls\n44"
    },
    {
      "page_number": 46,
      "text": "Example of POSIX API\n$ man read\n45"
    },
    {
      "page_number": 47,
      "text": "Example of POSIX API\n46"
    },
    {
      "page_number": 48,
      "text": "Standard C Library Example\n• C program invoking printf() library call, which calls write() system call\n47"
    },
    {
      "page_number": 49,
      "text": "System Call Implementation\n• Typically, each system call is associated\nwith an unique number (system call\nnumber)\n– System-call interface maintains a table\nindexed according to these numbers\n• The system call interface invokes the\nintended system call in OS kernel and\nreturns status of the system call and any\nreturn values\n• The caller needs to know nothing about how the system call is\nimplemented\n– Just needs to obey API and understand what OS will do for system call\n– Most details of OS interface are hidden from programmer by API\n48"
    },
    {
      "page_number": 50,
      "text": "System Call Parameter Passing\n• Often, more information (i.e., parameters) is required than simply the\nidentity of desired system call\n– The exact type and amount of information vary according to OS and system call\n• Three general methods used to pass parameters to the OS\n1) Simplest approach: pass the parameters in registers\n\nHowever, in some cases, there may be more parameters than registers\n2) Parameters are stored in a block (or table) in memory, and the address of the\nblock is passed as a parameter in a register\n This approach is taken by Linux and Solaris\n3) Parameters are pushed onto the stack by the user program and popped off\nthe stack by the operating system\n※ Block and stack methods do not limit the number or length of parameters being\npassed\n49"
    },
    {
      "page_number": 51,
      "text": "Parameter Passing via Memory Block\nX is the address of the memory\nblock storing parameters\n50"
    }
  ]
}'
WHERE course_id = '3bbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
  AND user_id = (SELECT id FROM app.users WHERE name = 'string3');
