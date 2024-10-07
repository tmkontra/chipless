// @ts-ignore

const Tailwind_PT = {
  button: {
    root: {
      class: [
        'text-gray',
        'hover:bg-gray-300 hover:border-gray-300 relative',
        'inline-flex items-center text-center overflow-hidden',
        'cursor-pointer px-3 py-2 text-base rounded-full'
      ]
    },
    label: {
      class: 'hidden'
    }
  },
  dialog: {
    root: {
      class:
        'relative flex flex-col max-h-90 transform scale-100 m-0 w-[80vw] z-40 rounded-lg shadow-lg border-0'
    },
    header: {
      class:
        'flex items-center justify-between flex-shrink-0 bg-white text-gray-800 border-t-0 rounded-tl-lg rounded-tr-lg p-6'
    },
    headerTitle: {
      class: 'font-bold text-lg'
    },
    headerIcons: {
      class: 'flex items-center'
    },
    closeButton: {
      class: [
        'flex items-center justify-center overflow-hidden relative',
        'w-8 h-8 text-gray-500 border-0 bg-transparent rounded-full transition duration-200 ease-in-out mr-2 last:mr-0',
        'hover:text-gray-700 hover:border-transparent hover:bg-gray-200',
        'focus:outline-none focus:outline-offset-0 focus:shadow-[0_0_0_0.2rem_rgba(191,219,254,1)]' // focus
      ]
    },
    closeButtonIcon: {
      class: 'w-4 h-4 inline-block'
    },
    content: {
      class: 'overflow-y-auto bg-white text-gray-700 px-6 pb-8 pt-0 rounded-bl-lg rounded-br-lg'
    },
    mask: {
      class:
        'fixed top-0 left-0 w-full h-full flex items-center justify-center pointer-events-auto bg-black bg-opacity-40 transition duration-200 z-20'
    }
  }
}

export default Tailwind_PT
