import matplotlib.pyplot as plt
import os

def pressureCD(data, save_dir):
    if not os.path.exists(save_dir):
        os.makedirs(save_dir)

    plt.figure()
    plt.plot(data)
    plt.title('data')
    plt.xlabel('x')
    plt.ylabel('y')
    plt.savefig(os.path.join(save_dir, 'figure1.png'))
    plt.close()
