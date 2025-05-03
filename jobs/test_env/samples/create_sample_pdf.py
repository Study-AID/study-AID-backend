#!/usr/bin/env python3

import argparse

from reportlab.lib.colors import black
from reportlab.lib.enums import TA_JUSTIFY, TA_LEFT, TA_CENTER
from reportlab.lib.pagesizes import letter
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, PageBreak


def parse_arguments():
    parser = argparse.ArgumentParser(description='Generate a sample PDF lecture file for testing')
    parser.add_argument('--output', default='sample_lecture.pdf', help='Output PDF file path')
    return parser.parse_args()


def generate_sample_pdf(output_path):
    """Generate a sample PDF file with lecture content"""
    # Create the document
    doc = SimpleDocTemplate(
        output_path,
        pagesize=letter,
        rightMargin=72,
        leftMargin=72,
        topMargin=72,
        bottomMargin=72
    )

    # Create styles
    styles = getSampleStyleSheet()
    styles.add(ParagraphStyle(
        name='Justify',
        alignment=TA_JUSTIFY,
        fontName='Helvetica',
        fontSize=12,
        leading=14
    ))

    # Title style
    title_style = ParagraphStyle(
        name='Title',
        parent=styles['Heading1'],
        alignment=TA_CENTER,
        fontName='Helvetica-Bold',
        fontSize=18,
        spaceAfter=24
    )

    # Heading styles
    heading1_style = styles['Heading1']
    heading2_style = styles['Heading2']

    # Normal text style
    normal_style = styles['Normal']

    # Create the content
    content = []

    # Title
    content.append(Paragraph("Introduction to Computer Science", title_style))
    content.append(Paragraph("Lecture 1: Fundamentals of Programming", title_style))
    content.append(Spacer(1, 12))

    # Introduction
    content.append(Paragraph("1. Introduction", heading1_style))
    content.append(Paragraph(
        "Computer science is the study of computation, automation, and information. "
        "Computer science spans theoretical disciplines (such as algorithms, theory of computation, "
        "and information theory) to practical disciplines (including the design and implementation "
        "of hardware and software). In this course, we will explore the fundamentals of programming "
        "and computational thinking, which form the foundation of computer science.",
        normal_style
    ))
    content.append(Spacer(1, 12))

    # What is Programming
    content.append(Paragraph("2. What is Programming?", heading1_style))
    content.append(Paragraph(
        "Programming is the process of creating a set of instructions that tell a computer how to perform "
        "a task. Programming can be done using a variety of computer programming languages, such as Python, "
        "Java, C++, and many others.",
        normal_style
    ))
    content.append(Spacer(1, 12))

    content.append(Paragraph("2.1 Key Programming Concepts", heading2_style))
    content.append(Paragraph(
        "• Variables: A variable is a storage location, paired with an associated symbolic name, which contains "
        "a value. Variables in programming are analogous to 'containers' that hold information.",
        normal_style
    ))
    content.append(Paragraph(
        "• Data Types: A data type is a classification of data which tells the compiler or interpreter how the "
        "programmer intends to use the data. Common data types include integers, floating-point numbers, characters, "
        "and strings.",
        normal_style
    ))
    content.append(Paragraph(
        "• Control Structures: Control structures are programming constructs that allow for the control of the flow "
        "of execution. Examples include conditionals (if-else statements) and loops (for and while loops).",
        normal_style
    ))
    content.append(Paragraph(
        "• Functions: A function is a block of organized, reusable code that is used to perform a single, related "
        "action. Functions provide better modularity for your application and reuse of code.",
        normal_style
    ))
    content.append(Spacer(1, 12))

    # Add a page break
    content.append(PageBreak())

    # Algorithms
    content.append(Paragraph("3. Introduction to Algorithms", heading1_style))
    content.append(Paragraph(
        "An algorithm is a finite sequence of well-defined, computer-implementable instructions, typically to solve "
        "a class of problems or to perform a computation. Algorithms are always unambiguous and are used as specifications "
        "for performing calculations, data processing, automated reasoning, and other tasks.",
        normal_style
    ))
    content.append(Spacer(1, 12))

    content.append(Paragraph("3.1 Characteristics of Algorithms", heading2_style))
    content.append(Paragraph(
        "• Input: An algorithm has input values from a specified set.",
        normal_style
    ))
    content.append(Paragraph(
        "• Output: An algorithm produces output values from a specified set. The output values are related to the input values.",
        normal_style
    ))
    content.append(Paragraph(
        "• Definiteness: Each instruction must be clear and unambiguous.",
        normal_style
    ))
    content.append(Paragraph(
        "• Finiteness: The algorithm must terminate after a finite number of steps.",
        normal_style
    ))
    content.append(Paragraph(
        "• Effectiveness: Each instruction must be basic enough to be carried out by a person using only pencil and paper.",
        normal_style
    ))
    content.append(Spacer(1, 12))

    content.append(Paragraph("3.2 Algorithm Analysis", heading2_style))
    content.append(Paragraph(
        "The analysis of algorithms is the determination of the amount of resources (such as time and storage) "
        "necessary to execute them. Most algorithms are designed to work with inputs of arbitrary length. "
        "Usually, the efficiency or running time of an algorithm is stated as a function relating the input "
        "length to the number of steps, known as time complexity, or volume of memory, known as space complexity.",
        normal_style
    ))
    content.append(Spacer(1, 12))

    # Add another page break
    content.append(PageBreak())

    # Programming Languages
    content.append(Paragraph("4. Introduction to Programming Languages", heading1_style))
    content.append(Paragraph(
        "A programming language is a formal language comprising a set of instructions that produce various kinds "
        "of output. Programming languages are used to implement algorithms.",
        normal_style
    ))
    content.append(Spacer(1, 12))

    content.append(Paragraph("4.1 Types of Programming Languages", heading2_style))
    content.append(Paragraph(
        "• Low-level languages: These languages provide little or no abstraction from a computer's instruction set architecture. "
        "They include machine code and assembly language.",
        normal_style
    ))
    content.append(Paragraph(
        "• High-level languages: These languages enable a programmer to write programs that are more independent of a "
        "particular type of computer. They are more abstract and easier to use than low-level languages. Examples "
        "include Python, Java, C++, and many others.",
        normal_style
    ))
    content.append(Paragraph(
        "• Scripting languages: These are high-level programming languages that are interpreted rather than compiled. "
        "Examples include JavaScript, PHP, and Python.",
        normal_style
    ))
    content.append(Spacer(1, 12))

    content.append(Paragraph("4.2 Programming Paradigms", heading2_style))
    content.append(Paragraph(
        "• Imperative programming: This paradigm uses statements that change a program's state. It focuses on how "
        "to achieve a goal step-by-step.",
        normal_style
    ))
    content.append(Paragraph(
        "• Declarative programming: This paradigm expresses the logic of a computation without describing its control flow. "
        "It focuses on what the program should accomplish, rather than how to accomplish it.",
        normal_style
    ))
    content.append(Paragraph(
        "• Object-oriented programming: This paradigm is based on the concept of 'objects', which can contain data and code. "
        "Data in the form of fields, and code, in the form of procedures.",
        normal_style
    ))
    content.append(Paragraph(
        "• Functional programming: This paradigm treats computation as the evaluation of mathematical functions and avoids "
        "changing-state and mutable data.",
        normal_style
    ))
    content.append(Spacer(1, 12))

    # Add another page break
    content.append(PageBreak())

    # Conclusion
    content.append(Paragraph("5. Conclusion", heading1_style))
    content.append(Paragraph(
        "In this introductory lecture, we have covered the fundamental concepts of computer science and programming. "
        "We have explored what programming is, the key concepts in programming, introduction to algorithms, and "
        "different types of programming languages and paradigms. In the next lecture, we will delve deeper into "
        "the specifics of a particular programming language and start writing our first programs.",
        normal_style
    ))
    content.append(Spacer(1, 12))

    content.append(Paragraph("6. References", heading1_style))
    content.append(Paragraph(
        "1. Introduction to Algorithms by Cormen, Leiserson, Rivest, and Stein",
        normal_style
    ))
    content.append(Paragraph(
        "2. Structure and Interpretation of Computer Programs by Abelson and Sussman",
        normal_style
    ))
    content.append(Paragraph(
        "3. The Art of Computer Programming by Donald Knuth",
        normal_style
    ))

    # Build the PDF
    doc.build(content)
    print(f"Generated sample PDF at: {output_path}")


def main():
    args = parse_arguments()
    generate_sample_pdf(args.output)


if __name__ == "__main__":
    main()
