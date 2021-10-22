const btns = document.querySelectorAll('button');
for(i = 0; i < btns.lenght; i++ ) {
    btns[i].addEventListener('click', print_main(btns[i]));
}

function print_main(button) {
    let main_pane = document.querySelector('.file-preview');
    // remove all children nodes  of main_pane
    while(main_pane.childElementCount > 0) {
        main_pane.removeChild(main_pane.first_child);
    }
    const notice = document.createElement('p');
    const msg = document.createTextNode("You clicked on " + button.textContent + "!");
    notice.appendChild(msg);
    main_pane.appendChild(notice);
}
